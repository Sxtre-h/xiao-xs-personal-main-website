package com.sxtreh.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageLocationConstant {

    @Value("${sxtreh.netdisk.file.path.avatar}")
    public String AVATAR_PATH;

    @Value("${sxtreh.netdisk.file.path.file}")
    public String FILE_PATH;

    @Value("${sxtreh.netdisk.file.path.temp}")
    public String TEMP_PATH;

    @Value("${sxtreh.netdisk.file.path.split}")
    public String SPLIT;


}
