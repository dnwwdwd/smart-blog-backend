package com.burger.smartblog.model.dto.article;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

@Data
public class ArticleRequest extends PageRequest {

    private String title;

}
