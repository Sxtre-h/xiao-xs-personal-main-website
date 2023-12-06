package com.sxtreh.netdisk.controller;

import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.UserFile;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.exception.ParameterErrorException;
import com.sxtreh.netdisk.annotation.ParameterCheck;
import com.sxtreh.netdisk.service.NetDiskService;
import com.sxtreh.result.Result;
import com.sxtreh.vo.UserFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * 网盘功能：文件CRUD、上传、下载
 * TODO 负载均衡会导致文件分片上传到不同服务器，如何保证文件上传到同一个服务器
 */
@Slf4j
@RestController
@RequestMapping("/netdisk")
public class NetDiskController {
    @Autowired
    private NetDiskService netDiskService;

    /**
     * 创建用户时初始化其网盘根目录，仅限微服务调用
     *
     * @param userFile
     * @return
     */
    @PostMapping("/files/initial")
    public Long initialNetDiskRootCatalog(@RequestBody UserFile userFile) {
        return netDiskService.initialNetDiskRootCatalog(userFile);
    }

    /**
     * 创建目录
     *
     * @param userFileDTO 新增用户目录参数
     * @return 成功
     */
    @ParameterCheck(rule = ParameterRuleType.NET_DISK_CATALOG_SAVE)
    @PostMapping("/files")
    public Result<UserFileVO> saveCatalog(@RequestBody UserFileDTO userFileDTO) {
        netDiskService.saveCatalog(userFileDTO);
        return Result.success();
    }

    /**
     * 删除文件
     *
     * @param userFileDTO 删除文件的ID
     * @return 成功
     */
    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_DELETE)
    @DeleteMapping("/files")
    public Result<UserFileVO> deleteFile(@RequestBody UserFileDTO userFileDTO) {
        netDiskService.deleteFile(userFileDTO.getFileId());
        return Result.success();
    }

    /**
     * 修改文件
     *
     * @param userFileDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_MODIFY)
    @PutMapping("/files")
    public Result<UserFileVO> modifyFile(@RequestBody UserFileDTO userFileDTO) {
        netDiskService.modifyFile(userFileDTO);
        return Result.success();
    }

    /**
     * 查找当前目录所有文件
     *
     * @param catalogId
     * @return 目录文件 和 子文件
     */

    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_LIST)
    @GetMapping("/files/{catalogId}")
    public Result<UserFileVO> listFile(@PathVariable("catalogId") Long catalogId) {
        List<UserFile> userFiles = netDiskService.listFile(catalogId);
        //从查询结果中将目录文件单独放在一个参数中
        for (UserFile userFile : userFiles) {
            if (userFile.getId().equals(catalogId)) {
                userFiles.remove(userFile);
                UserFileVO userFileVO = new UserFileVO(userFile, userFiles.size(), userFiles);
                return Result.success(userFileVO);
            }
        }
        throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
    }

    /**
     * 文件上传
     *
     * @param file
     * @param transFileId
     * @param fileMD5
     * @param catalogId
     * @param chunkIndex
     * @param chunks
     * @return
     */

    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_UPLOAD)
    @PostMapping("/files/upload")
    public Result<UserFileVO> uploadFile(MultipartFile file, String fileOriginName, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks) {
        if (chunkIndex >= chunks || chunkIndex < 0) {
            throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
        }
        netDiskService.uploadFile(file, fileOriginName, transFileId, fileMD5, catalogId, chunkIndex, chunks);
        return Result.success();
    }

    /**
     * 文件下载
     *
     * @param ids
     * @return
     */

    //TODO 返回下载链接而不是直接返回文件，这样方便分享文件等操作。同时可能限流负载均衡等操作更简单？
    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_DOWNLOAD)
    @GetMapping("/files/download")
    public ResponseEntity<InputStreamResource> downloadFiles(@RequestParam List<Long> ids) {
        List<String> filePaths = netDiskService.downloadFiles(ids);
        //如果没有找到文件
        if (filePaths.isEmpty()) throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        //找到了返回数据流
        File file = new File(filePaths.get(0));

        InputStreamResource resource;
        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            //路径位置文件被其他方式删了！！
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    /**
     * 分享文件，返回分享码
     *
     * @param userFileDTO
     * @return
     */
    @Cacheable(cacheNames = "fileShare", key = "#userFileDTO.fileId")//保证一个文件只有一个分享码
    @ParameterCheck(rule = ParameterRuleType.NET_DISK_FILE_SHARE)
    @PostMapping("/files/shares")
    public Result shareFiles(@RequestBody UserFileDTO userFileDTO) {
        String shareCode = netDiskService.shareFiles(userFileDTO.getFileId());
        return Result.success(shareCode);
    }

    @ParameterCheck(rule = ParameterRuleType.NET_DISK_GET_SHARED_FILES)
    @PutMapping("/files/shares")
    public Result getSharedFiles(@RequestBody UserFileDTO userFileDTO) {
        netDiskService.getSharedFiles(userFileDTO);
        return Result.success();
    }
}
