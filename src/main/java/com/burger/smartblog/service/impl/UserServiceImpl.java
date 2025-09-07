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
import com.burger.smartblog.model.entity.User;
import com.burger.smartblog.model.vo.LoginUserVO;
import com.burger.smartblog.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
                .eq(User::getUserPassword, encryptPassword)
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

    private LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

}




