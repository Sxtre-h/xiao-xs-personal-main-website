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
@TableName("t_note")
public class Note {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    Long userId;

    Long catalogId;

    String noteName;

    String noteBody;

    String status;

    Integer noteOrder;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtCreate;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtUpdate;
}
