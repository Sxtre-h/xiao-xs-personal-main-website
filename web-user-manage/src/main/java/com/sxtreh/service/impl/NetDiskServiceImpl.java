package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.constant.FileStorageLocationConstant;
import com.sxtreh.constant.FileTypeConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.FileInfo;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.entity.User;
import com.sxtreh.entity.UserFile;
import com.sxtreh.enumeration.FileType;
import com.sxtreh.mapper.FileInfoMapper;
import com.sxtreh.mapper.NetDiskMapper;
import com.sxtreh.mapper.UserMapper;
import com.sxtreh.result.UploadResult;
import com.sxtreh.service.NetDiskService;
import com.sxtreh.utils.UploadUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetDiskServiceImpl implements NetDiskService {
    @Autowired
    private NetDiskMapper netDiskMapper;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public void saveCatalog(UserFileDTO userFileDTO) {
        UserFile userFile = new UserFile();
        BeanUtils.copyProperties(userFileDTO, userFile);

        userFile.setUserId(BaseContext.getCurrentId());
        userFile.setFileType(FileTypeConstant.CATALOG);

        netDiskMapper.insert(userFile);
    }

    @Override
    public void deleteFile(Long fileId) {

        UserFile userFile = netDiskMapper.selectById(fileId);
        if(userFile == null) {
            return;
        }
        if(userFile.getFileType().equals(FileTypeConstant.CATALOG)){
            netDiskMapper.deleteById(fileId);
            //TODO 删除子文件
        }
        if(userFile.getFileType().equals(FileTypeConstant.FILE)){
            netDiskMapper.deleteById(fileId);
            //修改文件信息表文件指针数
            fileInfoMapper.decreasePointNumber(userFile.getFileId());
            //释放用户空间
            User user = userMapper.selectById(BaseContext.getCurrentId());
            user.setUserSpaceRemain(user.getUserSpaceRemain() + userFile.getFileSize());
            userMapper.updateById(user);
        }


    }

    @Override
    public void modifyFile(UserFileDTO userFileDTO) {
        UserFile userFile = new UserFile();
        BeanUtils.copyProperties(userFileDTO, userFile);

        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFile::getId, userFileDTO.getFileId());
        netDiskMapper.update(userFile, queryWrapper);

    }

    @Override
    public List<UserFile> listFile(Long catalogId) {
//        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(UserFile::getFilePid, catalogId);
//        //获取子文件
//        List<UserFile> userFiles = netDiskMapper.selectList(queryWrapper);
//        //将目录文件放到数组尾部，提升性能
//        userFiles.add(netDiskMapper.selectById(catalogId));
        //将两次数据查询合并为一次
        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFile::getId, catalogId);
        queryWrapper.or(wrapper -> wrapper.eq(UserFile::getFilePid, catalogId));
        //获取文件
        List<UserFile> userFiles = netDiskMapper.selectList(queryWrapper);
        return userFiles;

    }

    @Override
    public void uploadFile(MultipartFile file, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks) {
        User user = userMapper.selectById(BaseContext.getCurrentId());
        if(user.getUserSpaceRemain() < file.getSize()) {
            throw new RuntimeException("空间不足");
        }

        //TODO 验证MD5
//        FileInfo fileInfo = fileInfoMapper.selectByMD5(fileMD5);
        FileInfo fileInfo = fileInfoMapper.selectByMD5("-1");
        //不存在相同文件则上传
        if(fileInfo == null) {
            UploadResult result;
            //上传文件
            try {
                result = UploadUtil.upload(transFileId, FileStorageLocationConstant.FILE_PATH, file, chunkIndex, chunks);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //文件还没上传完毕,返回
            if (result.getCode().equals(0)) {
                return;
            }
            //文件上传完毕,文件信息表添加文件
            fileInfo = FileInfo.builder()
                    .fileName(result.getFileName())//UUID
                    .fileSize(result.getFileSize())
                    .fileUrl(result.getFileUrl())
                    .pointerNumber(1)
                    .hashValue(fileMD5)
                    .build();
            fileInfoMapper.insertAndGetId(fileInfo);
        }
        else {
            //已经存在相同文件则更新文件信息表中文件的引用值
            fileInfoMapper.increasePointNumber(fileInfo.getId());
        }
        //用户文件表中添加文件索引
        UserFile userFile = UserFile.builder()
                .userId(BaseContext.getCurrentId())
                .fileId(fileInfo.getId())
                .filePid(catalogId)
                .fileName(file.getOriginalFilename())//上传的文件名, 不是getName();
                .fileSize(fileInfo.getFileSize())
                .fileType(FileTypeConstant.FILE)
                .fileUrl(fileInfo.getFileUrl())
                .build();
        netDiskMapper.insert(userFile);

        user.setUserSpaceRemain(user.getUserSpaceRemain() - file.getSize());
        userMapper.updateById(user);
    }

    @Override
    public List<String> downloadFiles(List<Long> ids) {
        List<UserFile> userFiles = netDiskMapper.selectBatchIds(ids);
        List<String> paths = new ArrayList<>();
        for(UserFile file : userFiles){
            paths.add(file.getFileUrl());
        }
        return paths;
    }
}
