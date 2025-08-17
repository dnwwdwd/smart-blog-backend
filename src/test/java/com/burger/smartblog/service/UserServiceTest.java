package com.burger.smartblog.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *

 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private ArticleService articleService;

    @Test
    public void test() {
        System.out.println(userService.list());
    }

    @Test
    public void testArticle() {
        System.out.println(articleService.list());
    }


}
