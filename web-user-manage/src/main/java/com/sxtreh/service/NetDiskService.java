package com.sxtreh.service;

import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.UserFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NetDiskService {
    void saveCatalog(UserFileDTO userFileDTO);

    void deleteFile(Long fileId);

    void modifyFile(UserFileDTO userFileDTO);

    List<UserFile> listFile(Long catalogId);

    void uploadFile(MultipartFile file, String fileOriginName, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks);

    List<String> downloadFiles(List<Long> ids);

    String shareFiles(Long fileId);

    void getSharedFiles(UserFileDTO userFileDTO);
}
