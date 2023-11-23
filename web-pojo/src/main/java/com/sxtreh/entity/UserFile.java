package com.sxtreh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_net_disk_user_file")
public class UserFile {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    Long userId;

    Long fileId;

    Long filePid;

    String fileName;

    Long fileSize;

    Integer fileType;

    String fileUrl;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtCreate;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtUpdate;
}
