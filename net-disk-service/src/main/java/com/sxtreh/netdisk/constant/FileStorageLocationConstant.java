package com.sxtreh.netdisk.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 文件存储路径，为了区分部署系统，需要自动注入
 */
@Component
public class FileStorageLocationConstant {
    //自动注入不要使用static，会失败，可解决，但麻烦
    @Value("${sxtreh.netdisk.file.path.file}")
    public String AVATAR_PATH;
    @Value("${sxtreh.netdisk.file.path.file}")
    public String FILE_PATH;
    @Value("${sxtreh.netdisk.file.path.temp}")
    public String TEMP_PATH;
    @Value("${sxtreh.netdisk.file.path.split}")
    public String SPLIT;


}
