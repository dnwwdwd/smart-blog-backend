package com.burger.smartblog.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.model.dto.user.UserLoginRequest;
import com.burger.smartblog.model.vo.LoginUserVO;
import com.burger.smartblog.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Validated
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody @Validated UserLoginRequest userLoginRequest) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        StpUtil.logout();
        return ResultUtils.success(true);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() {
        return ResultUtils.success(userService.getLoginUser());
    }

}
