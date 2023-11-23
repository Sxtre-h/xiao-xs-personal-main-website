package com.sxtreh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFileDTO {
    Long fileId;

    Long filePid;

    String fileName;

    Long fileSize;

    Integer fileType;

}
