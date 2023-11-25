package com.sxtreh.utils;

import com.sxtreh.constant.FileStorageLocationConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.exception.FileUploadErrorException;
import com.sxtreh.result.Result;
import com.sxtreh.result.UploadResult;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 分片上传文件到本地
     *
     * @param fileId
     * @param storagePath
     * @param file
     * @param chunkIndex
     * @param chunks
     * @return 文件本地存储路径
     * @throws Exception
     */
    public static UploadResult upload(Long fileId, String storagePath, MultipartFile file, Integer chunkIndex, Integer chunks) throws Exception {
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
        //文件数足够时，尝试合并
        String newFileName = UUID.randomUUID().toString();
        String fileUrl = storagePath + newFileName;
        File[] files = dir.listFiles();
        if (files.length == chunks) {
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
                    } catch (Exception e) {
                        throw new FileUploadErrorException(MessageConstant.UPLOAD_ERROR);
                    } finally {
                        if (readFile != null)
                            readFile.close();
                    }
                }
                uploadResult.setCode(1);
                uploadResult.setFileName(newFileName);
                uploadResult.setFileUrl(fileUrl);
                uploadResult.setFileSize(targetFile.length());
                //全部写入后, 删除所有文件
                for (File df : files) {
                    df.delete();
                }
                dir.delete();
            } catch (Exception e) {
                throw new FileUploadErrorException(MessageConstant.UPLOAD_ERROR);
            } finally {
                if (writeFile != null) {
                    writeFile.close();
                }
            }
        }
        return uploadResult;
    }
    /**
     * 清理临时文件
     */
    public static void cleanTempFile(Long userId, Long fileId){
        String tempDir = FileStorageLocationConstant.TEMP_PATH + userId + fileId + "\\";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        for (File df : files) {
            df.delete();
        }
        dir.delete();
    }
}
