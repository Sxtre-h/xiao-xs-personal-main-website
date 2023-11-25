package com.sxtreh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_note_catalog")
public class NoteCatalog {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    Long userId;

    Integer catalogLevel;

    String catalogName;

    Long parentCatalogId;

    String status;

    Integer catalogOrder;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtCreate;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtUpdate;
}
