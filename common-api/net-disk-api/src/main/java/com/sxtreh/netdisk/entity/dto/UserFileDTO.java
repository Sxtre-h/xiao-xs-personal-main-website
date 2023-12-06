package com.sxtreh.netdisk.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFileDTO {
    //设计失误，这不是文件的实际id，而是用户文件id，注意区分
    Long fileId;

    Long filePid;

    String fileName;

    String shareCode;

}
