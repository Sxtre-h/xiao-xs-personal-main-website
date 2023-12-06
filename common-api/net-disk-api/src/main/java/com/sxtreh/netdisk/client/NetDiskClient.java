package com.sxtreh.netdisk.client;

import com.sxtreh.entity.UserFile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "net-disk-service")
public interface NetDiskClient {
    /**
     * 创建用户初始云盘目录
     * @param userFileDTO
     * @return
     */
    @PostMapping("/netdisk/files/initial")
    Long initialNetDiskRootCatalog(@RequestBody UserFile userFileDTO);

}
