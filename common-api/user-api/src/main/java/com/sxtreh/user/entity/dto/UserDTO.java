package com.sxtreh.user.entity.dto;


import lombok.Data;

@Data
public class UserDTO {
    //注册码
    String registerCode;

    String userName;

    String loginName;

    String password;

    //简介
    String profile;

    //头像url
    String avatar;

}