package com.burger.smartblog.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.exception.BusinessException;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MustLoginAop {

    @Around("@annotation(com.burger.smartblog.annotation.MustLogin)")
    public Object mustLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Object loginId = StpUtil.getLoginId();
        if (ObjectUtils.isEmpty(loginId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return joinPoint.proceed();
    }

}
