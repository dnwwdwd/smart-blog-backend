package com.burger.smartblog.service;

import com.burger.smartblog.model.dto.user.UserLoginRequest;
import com.burger.smartblog.model.dto.user.UserRegisterRequest;
import com.burger.smartblog.model.dto.user.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.entity.User;
import com.burger.smartblog.model.vo.LoginUserVO;
import com.burger.smartblog.model.vo.PublicUserVO;
import com.burger.smartblog.model.vo.UserUpdateResponse;

/**
 * @author hejiajun
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-08-10 11:45:33
 */
public interface UserService extends IService<User> {

    LoginUserVO userLogin(UserLoginRequest userLoginRequest);

    LoginUserVO getLoginUser();

    void userRegister(UserRegisterRequest userRegisterRequest);

    PublicUserVO getPublicAuthorProfile();

    UserUpdateResponse updateCurrentUser(UserUpdateRequest request);

}
