package com.burger.smartblog.controller;

import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.manager.AliOSSManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.image.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/image")
@Tag(name = "图片接口")
public class ImageController {

    @Resource
    private AliOSSManager aliOSSManager;

    @Resource
    private ImageModel imageModel;

    @PostMapping("/upload")
    public BaseResponse<String> uploadImage(@RequestPart MultipartFile file) {
        String url = null;
        try {
            url = aliOSSManager.upload(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        }
        System.out.println("图片链接：" + url);
        return ResultUtils.success(url);
    }

    @RequestMapping("/generate")
    public String image(String input) {
        ImageOptions options = ImageOptionsBuilder.builder()
                .model("dall-e-3")
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(input, options);
        ImageResponse response = imageModel.call(imagePrompt);
        String imageUrl = response.getResult().getOutput().getUrl();

        return "redirect:" + imageUrl;
    }

}
