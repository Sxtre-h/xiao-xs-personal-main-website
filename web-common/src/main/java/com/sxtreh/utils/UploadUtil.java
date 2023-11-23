package com.sxtreh.utils;

import com.sxtreh.constant.FileStorageLocationConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.result.Result;
import com.sxtreh.result.UploadResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class UploadUtil {


    /**
     *分片上传文件到本地
     * @param fileId
     * @param storagePath
     * @param file
     * @param chunkIndex
     * @param chunks
     * @return 文件本地存储路径
     * @throws Exception
     */
    public static UploadResult upload(Long fileId, String storagePath, MultipartFile file, Integer chunkIndex, Integer chunks) throws Exception {
        //TODO redis 判断分片是否存在 ： 需要？ 顺序上传，一点出错都不会上传后面的片
        UploadResult uploadResult = new UploadResult();
        uploadResult.setCode(0);
        //分片上传文件
        //临时文件目录
        String tempDir = FileStorageLocationConstant.TEMP_PATH + BaseContext.getCurrentId() + fileId + "\\";

        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String tempPathName = tempDir + chunkIndex.toString();
        file.transferTo(new File(tempPathName));
        //最后一片上传后，合并文件
        String newFileName = UUID.randomUUID().toString();
        String fileUrl = storagePath + newFileName;
        if (chunkIndex.equals(chunks)) {
            File[] files = dir.listFiles();
            File targetFile = new File(fileUrl);
            RandomAccessFile writeFile = null;
            try {
                writeFile = new RandomAccessFile(targetFile, "rw");
                byte[] buf = new byte[1024 * 10];
                for (int i = 0; i < files.length; ++i) {
                    int len = -1;
                    RandomAccessFile readFile = null;
                    try {
                        File chunkFile = files[i];
                        readFile = new RandomAccessFile(chunkFile, "r");
                        while ((len = readFile.read(buf)) != -1) {
                            writeFile.write(buf, 0, len);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (readFile != null)
                            readFile.close();
                    }
                }
                uploadResult.setCode(1);
                uploadResult.setFileName(newFileName);
                uploadResult.setFileUrl(fileUrl);
                uploadResult.setFileSize(targetFile.length());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (writeFile != null) {
                    writeFile.close();
                }
            }
        }
        return uploadResult;
    }
}
