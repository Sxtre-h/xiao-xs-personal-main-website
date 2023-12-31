package com.sxtreh.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {
    Long noteId;

    Long catalogId;

    String noteName;

    JSONObject noteBody;

    Integer noteOrder;

}
