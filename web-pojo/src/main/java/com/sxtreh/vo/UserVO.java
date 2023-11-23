package com.sxtreh.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    String userName;

    String loginName;

    String password;

    //简介
    String profile;

    //头像url
    String avatar;

    String token;

    //注册码
    String registerCode;

    Boolean isAdmin;

    //用户通知信息
    String msg;

    //用户网盘根目录ID
    Long userNetDiskRootId;
    //用户剩余网盘空间
    Long userSpaceRemain;
    //用户网盘总空间
    Long userSpaceTotal;

}
