package com.burger.smartblog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.mapper.UserMapper;
import com.burger.smartblog.model.dto.user.UserLoginRequest;
import com.burger.smartblog.model.dto.user.UserRegisterRequest;
import com.burger.smartblog.model.dto.user.UserUpdateRequest;
import com.burger.smartblog.model.entity.User;
import com.burger.smartblog.model.vo.LoginUserVO;
import com.burger.smartblog.model.vo.PublicUserVO;
import com.burger.smartblog.model.vo.UserUpdateResponse;
import com.burger.smartblog.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author hejiajun
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "burger";

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        wrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(wrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        StpUtil.login(user.getId());
        LoginUserVO loginUserVO = BeanUtil.copyProperties(user, LoginUserVO.class);
        loginUserVO.setToken(StpUtil.getTokenInfo().getTokenValue());
        return loginUserVO;
    }

    @Override
    public LoginUserVO getLoginUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        return getLoginUserVO(user);
    }

    @Override
    public void userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        Long count = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
        }
    }

    @Override
    public PublicUserVO getPublicAuthorProfile() {
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getIsDelete, 0)
                .eq(User::getUserRole, "admin")
                .orderByAsc(User::getCreateTime)
                .last("LIMIT 1");
        User user = this.getOne(adminWrapper, false);
        if (user == null) {
            LambdaQueryWrapper<User> fallback = new LambdaQueryWrapper<>();
            fallback.eq(User::getIsDelete, 0)
                    .orderByAsc(User::getCreateTime)
                    .last("LIMIT 1");
            user = this.getOne(fallback, false);
        }
        if (user == null) {
            return null;
        }
        PublicUserVO vo = new PublicUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserUpdateResponse updateCurrentUser(UserUpdateRequest request) {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user == null || Objects.equals(user.getIsDelete(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        boolean changed = false;
        boolean needLogout = false;

        if (StringUtils.isNotBlank(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            user.setUsername(request.getUsername().trim());
            changed = true;
        }
        if (request.getUserAvatar() != null && !Objects.equals(request.getUserAvatar(), user.getUserAvatar())) {
            user.setUserAvatar(request.getUserAvatar());
            changed = true;
        }
        if (request.getProfile() != null && !Objects.equals(request.getProfile(), user.getProfile())) {
            user.setProfile(request.getProfile());
            changed = true;
        }

        if (StringUtils.isNotBlank(request.getUserAccount()) && !request.getUserAccount().equals(user.getUserAccount())) {
            ensurePasswordVerified(user, request.getCurrentPassword());
            long exists = this.lambdaQuery()
                    .eq(User::getUserAccount, request.getUserAccount())
                    .ne(User::getId, userId)
                    .count();
            if (exists > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已被占用");
            }
            user.setUserAccount(request.getUserAccount());
            changed = true;
            needLogout = true;
        }

        if (StringUtils.isNotBlank(request.getNewPassword())) {
            ensurePasswordVerified(user, request.getCurrentPassword());
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + request.getNewPassword()).getBytes());
            if (encryptPassword.equals(user.getUserPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
            }
            user.setUserPassword(encryptPassword);
            changed = true;
            needLogout = true;
        }

        if (!changed) {
            return new UserUpdateResponse(false);
        }

        boolean updated = this.updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败，请稍后重试");
        }

        if (needLogout) {
            StpUtil.logout();
        }
        return new UserUpdateResponse(needLogout);
    }

    private LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    private void ensurePasswordVerified(User user, String currentPassword) {
        if (StringUtils.isBlank(currentPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先验证当前密码");
        }
        String encrypted = DigestUtils.md5DigestAsHex((SALT + currentPassword).getBytes());
        if (!encrypted.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前密码不正确");
        }
    }

}




