package com.burger.smartblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.model.entity.Comment;
import com.burger.smartblog.model.vo.CommentAdminVo;
import org.apache.ibatis.annotations.Param;

/**
 * @author hejiajun
 * @description 针对表【comment】的数据库操作Mapper
 * @createDate 2025-08-10 11:45:33
 * @Entity com.burger.smartblog.model.entity.Comment
 */
public interface CommentMapper extends BaseMapper<Comment> {

    Page<CommentAdminVo> selectCommentAdminPage(Page<?> page,
                                                @Param("articleId") Long articleId,
                                                @Param("searchKeyword") String searchKeyword);
}




