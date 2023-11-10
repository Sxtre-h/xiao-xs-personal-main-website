package com.sxtreh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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