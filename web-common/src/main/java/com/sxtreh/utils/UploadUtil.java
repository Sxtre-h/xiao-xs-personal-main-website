package com.sxtreh.utils;

import com.sxtreh.constant.MessageConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.exception.FileUploadErrorException;
import com.sxtreh.result.UploadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

@Slf4j
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
    public static UploadResult upload(Long fileId, String storagePath, MultipartFile file, Integer chunkIndex, Integer chunks, String split, String tempPath) throws Exception {
        UploadResult uploadResult = new UploadResult();
        uploadResult.setCode(0);
        //分片上传文件
        //临时文件目录
        String tempDir = tempPath + BaseContext.getCurrentId() + fileId + split;
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String tempPathName = tempDir + chunkIndex.toString();
        //保险起见，Linux系统先创建文件
        log.info("创建临时文件");
        File linuxFile = new File(tempPathName);
        if(!linuxFile.exists()){
            linuxFile.createNewFile();
        }
        //Linux
        FileUtils.copyInputStreamToFile(file.getInputStream(),linuxFile);
        //Windows
//        file.transferTo(file1);
//        file.transferTo(new File(tempPathName));

        //文件数足够时，尝试合并
        String newFileName = UUID.randomUUID().toString();
        String fileUrl = storagePath + newFileName;
        File[] files = dir.listFiles();
        //files默认随机排序，需要手动排序，否则组装时会使文件错误。
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.valueOf(o1.getName())-Integer.valueOf(o2.getName());
            }
        });
        if (files.length == chunks) {
            log.info("开始合并");
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
                        log.error(e.getMessage());
                        throw new FileUploadErrorException(MessageConstant.UPLOAD_MERGE_ERROR);
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
                log.error(e.getMessage());
                throw new FileUploadErrorException(MessageConstant.UPLOAD_MERGE_ERROR);
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
    public static void cleanTempFile(Long userId, Long fileId, String split, String tempPath){
        String tempDir = tempPath + userId + fileId + split;
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
