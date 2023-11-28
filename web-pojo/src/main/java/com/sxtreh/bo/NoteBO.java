package com.sxtreh.bo;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 拆分Note类的noteBody字段为字符串数组，封装后返回给前端
 * 分隔符号 "\t"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteBO {
    Long id;

    Long catalogId;

    String noteName;

    JSONObject noteBody;

    Integer noteOrder;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtCreate;

    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime gmtUpdate;
}
