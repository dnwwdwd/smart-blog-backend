package com.burger.smartblog.service;

import com.burger.smartblog.model.dto.user.UserLoginRequest;
import com.burger.smartblog.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.vo.LoginUserVO;

/**
 * @author hejiajun
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-08-10 11:45:33
 */
public interface UserService extends IService<User> {

    LoginUserVO userLogin(UserLoginRequest userLoginRequest);

    LoginUserVO getLoginUser();
}
