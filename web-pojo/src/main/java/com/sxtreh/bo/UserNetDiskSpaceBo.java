package com.sxtreh.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用于用户模块和网盘模块MQ消息通信，修改用户网盘空间信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNetDiskSpaceBo implements Serializable {
    Long userId;
    Long fileSize;
    String modifyType;
}
