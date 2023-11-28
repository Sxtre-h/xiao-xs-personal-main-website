package com.sxtreh.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记表格列对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteColDTO {
    Long catalogId;

    String noteColName;
}
