package com.burger.smartblog.model.vo;

import com.burger.smartblog.model.entity.FriendLink;
import com.burger.smartblog.model.entity.SocialLink;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FriendLinkVo extends FriendLink implements Serializable {

    private List<SocialLink> socialLinks;

}
