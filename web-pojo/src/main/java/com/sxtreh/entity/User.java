package com.sxtreh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_info")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    String userName;

    String loginName;

    String password;

    //简介，如果字段为空或者不存在，表中字段会写入空
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String profile;

    //头像url
    String avatar;

    //账号状态
    String status;

    Boolean isAdmin;

    //用户网盘根目录ID
    Long userNetDiskRootId;
    //用户剩余网盘空间
    Long userSpaceRemain;
    //用户网盘总空间
    Long userSpaceTotal;

    //用户邀请人ID
    Long inviteUserId;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtCreate;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtUpdate;
}
