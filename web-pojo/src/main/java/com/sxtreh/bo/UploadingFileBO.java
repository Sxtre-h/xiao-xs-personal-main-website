package com.sxtreh.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储正在上传的文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadingFileBO implements Serializable {
    //文件所属用户
    Long userId;
    //文件标识
    Long fileId;
    //最近更新时间
    LocalDateTime recentUpdateTime;
}
