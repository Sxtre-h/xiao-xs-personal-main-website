package com.sxtreh.result;

import lombok.Data;

@Data
public class UploadResult {

    private Integer code;

    private String fileName;

    private Long fileSize;

    private String fileUrl;
}
