package com.sxtreh.controller;

import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.UserFile;
import com.sxtreh.exception.ParameterMissingException;
import com.sxtreh.result.Result;
import com.sxtreh.service.NetDiskService;
import com.sxtreh.vo.UserFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@RestController
@RequestMapping("/netdisk")
public class NetDiskController {
    @Autowired
    private NetDiskService netDiskService;

    /**
     * 创建目录
     *
     * @param userFileDTO
     * @return
     */
    @RequireLogin
    @PostMapping("files")
    public Result<UserFileVO> saveCatalog(@RequestBody UserFileDTO userFileDTO) {
        if (userFileDTO.getFilePid() == null || userFileDTO.getFileName() == null) {
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        netDiskService.saveCatalog(userFileDTO);
        return Result.success();
    }

    /**
     * 删除文件
     *
     * @param userFileDTO
     * @return
     */
    @RequireLogin
    @DeleteMapping("/files")
    public Result<UserFileVO> deleteFile(@RequestBody UserFileDTO userFileDTO) {
        if (userFileDTO.getFileId() == null) {
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        netDiskService.deleteFile(userFileDTO.getFileId());
        return Result.success();
    }

    /**
     * 修改文件
     *
     * @param userFileDTO
     * @return
     */
    @RequireLogin
    @PutMapping("/files")
    public Result<UserFileVO> modifyFile(@RequestBody UserFileDTO userFileDTO) {
        if (userFileDTO.getFileId() == null) {
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        if (!(userFileDTO.getFilePid().equals(null) && userFileDTO.getFileName().equals(null))) {
            netDiskService.modifyFile(userFileDTO);
        }
        return Result.success();
    }

    /**
     * 查找当前目录所有文件
     *
     * @param catalogId
     * @return 目录文件 和 子文件
     */
    @RequireLogin
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
        return Result.error(MessageConstant.UNKNOWN_ERROR);
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

    @RequireLogin
    @PostMapping("/files/upload")
    public Result<UserFileVO> uploadFile(MultipartFile file, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks) {

        netDiskService.uploadFile(file, transFileId, fileMD5, catalogId, chunkIndex, chunks);
        return Result.success();
    }

    /**
     * 文件下载
     *
     * @param ids
     * @return
     * @throws FileNotFoundException
     */

    //TODO 返回下载链接而不是直接返回文件，这样方便分享文件等操作（实际前端是不会写接收代码T^T）。同时可能限流负载均衡等操作更简单？
    @RequireLogin
    @GetMapping("/files/download")
    public ResponseEntity<InputStreamResource> downloadFiles(@RequestParam List<Long> ids) throws FileNotFoundException {
        List<String> filePaths = netDiskService.downloadFiles(ids);
        File file = new File(filePaths.get(0));
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }
}
