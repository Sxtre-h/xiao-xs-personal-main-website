package com.sxtreh.netdisk.task;

import com.sxtreh.bo.UploadingFileBO;
import com.sxtreh.netdisk.constant.FileStorageLocationConstant;
import com.sxtreh.utils.UploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;


/**
 * 每小时自动清理长时间未上传完毕且无请求的临时文件
 */
@Slf4j
@Component
public class CleanTempFileTask {
    @Autowired
    RedisTemplate redisTemplate;
    private String fileKey = "uploadingFile_*";

    @Autowired
    private FileStorageLocationConstant fileStorageLocationConstant;

    @Scheduled(cron = "0 0 0/1 * * ? ")
    public void cleanTimeOutFile() {
        Set fileKeys = redisTemplate.keys(fileKey);
        for (Object fileKey : fileKeys) {
            UploadingFileBO o = (UploadingFileBO) redisTemplate.opsForValue().get(fileKey);
            if (o != null) {
                if (o.getRecentUpdateTime().compareTo(LocalDateTime.now().plusSeconds(-10L)) < 0) {
                    log.info("文件{}超时,自动清理", o.getFileId());
                    //清理临时文件
                    UploadUtil.cleanTempFile(o.getUserId(), o.getFileId(), fileStorageLocationConstant.SPLIT, fileStorageLocationConstant.TEMP_PATH);
                    //清理文件缓存
                    redisTemplate.delete(fileKey);
                    //清理文件分片缓存
                    String chunkKey = "file_" + o.getUserId() + "_" + o.getFileId() + "_" + "*";
                    Set chunkKeys = redisTemplate.keys(chunkKey);
                    redisTemplate.delete(chunkKeys);
                }
            }
        }
    }
}
