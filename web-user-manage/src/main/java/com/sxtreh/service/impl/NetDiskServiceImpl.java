package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.bo.UploadingFileBO;
import com.sxtreh.constant.FileStorageLocationConstant;
import com.sxtreh.constant.FileTypeConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.*;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.exception.FileUploadErrorException;
import com.sxtreh.mapper.FileInfoMapper;
import com.sxtreh.mapper.NetDiskMapper;
import com.sxtreh.mapper.UserMapper;
import com.sxtreh.result.UploadResult;
import com.sxtreh.service.NetDiskService;
import com.sxtreh.utils.UploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class NetDiskServiceImpl implements NetDiskService {
    @Autowired
    private NetDiskMapper netDiskMapper;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void saveCatalog(UserFileDTO userFileDTO) {
        //判定父目录合法性
        UserFile fileParent = netDiskMapper.selectById(userFileDTO.getFilePid());
        if (fileParent == null
                || fileParent.getUserId() != BaseContext.getCurrentId()
                || !fileParent.getFileType().equals(FileTypeConstant.CATALOG)) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }

        UserFile userFile = new UserFile();
        BeanUtils.copyProperties(userFileDTO, userFile);

        userFile.setUserId(BaseContext.getCurrentId());
        userFile.setFileType(FileTypeConstant.CATALOG);

        netDiskMapper.insert(userFile);
    }

    @Override
    public void deleteFile(Long fileId) {

        //检查参数合法性
        UserFile userFile = netDiskMapper.selectById(fileId);
        if (userFile == null || userFile.getUserId() != BaseContext.getCurrentId()) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //删除目录或文件
        if (userFile.getFileType().equals(FileTypeConstant.CATALOG)) {
            LambdaQueryWrapper<UserFile> catalogQueryWrapper = new LambdaQueryWrapper<>();
            catalogQueryWrapper.eq(UserFile::getId, fileId)
                    .eq(UserFile::getUserId, BaseContext.getCurrentId());
            //删除目录
            netDiskMapper.delete(catalogQueryWrapper);
            //递归删除目录下所有文件
            LambdaQueryWrapper<UserFile> fileQueryWrapper = new LambdaQueryWrapper<>();
            fileQueryWrapper.eq(UserFile::getFilePid, fileId)
                    .eq(UserFile::getUserId, BaseContext.getCurrentId());
            List<UserFile> subFiles = netDiskMapper.selectList(fileQueryWrapper);
            for (UserFile subFile : subFiles) {
                deleteFile(subFile.getId());
            }
        } else if (userFile.getFileType().equals(FileTypeConstant.FILE)) {
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
        //如果是移动文件，判定父目录合法性
        if (userFileDTO.getFilePid() != null) {
            UserFile fileParent = netDiskMapper.selectById(userFileDTO.getFilePid());
            if (fileParent == null
                    || fileParent.getUserId() != BaseContext.getCurrentId()
                    || !fileParent.getFileType().equals(FileTypeConstant.CATALOG)) {
                throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
            }
        }
        UserFile userFile = new UserFile();
        BeanUtils.copyProperties(userFileDTO, userFile);

        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFile::getId, userFileDTO.getFileId())
                .eq(UserFile::getUserId, BaseContext.getCurrentId());
        netDiskMapper.update(userFile, queryWrapper);

    }

    @Override
    public List<UserFile> listFile(Long catalogId) {
        //这是不对的
        //queryWrapper.eq(UserFile::getId, catalogId)
        //    .eq(UserFile::getUserId, BaseContext.getCurrentId())
        //    .or(wrapper -> wrapper.eq(UserFile::getFilePid, catalogId));

        //查询目录本身和目录子文件
        LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
        //这么写不会被打死吧
        queryWrapper.eq(UserFile::getUserId, BaseContext.getCurrentId())//是用户的文件
                .and(wrapper -> {
                    wrapper.eq(UserFile::getId, catalogId)//目录本身
                            .or(subWrapper -> subWrapper.eq(UserFile::getFilePid, catalogId));//目录子文件
                });
        //获取文件
        List<UserFile> userFiles = netDiskMapper.selectList(queryWrapper);
        return userFiles;
    }

    @Override
    public void uploadFile(MultipartFile file, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks) {
        //判定父目录合法性
        UserFile fileParent = netDiskMapper.selectById(catalogId);
        if (fileParent == null
                || fileParent.getUserId() != BaseContext.getCurrentId()
                || !fileParent.getFileType().equals(FileTypeConstant.CATALOG)) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //判断用户剩余空间是否充足
        User user = userMapper.selectById(BaseContext.getCurrentId());
        if (user.getUserSpaceRemain() < file.getSize()) {
            throw new RuntimeException("空间不足");
        }
        //TODO 验证MD5
//        FileInfo fileInfo = fileInfoMapper.selectByMD5(fileMD5);
        FileInfo fileInfo = fileInfoMapper.selectByMD5("-1");
        //不存在相同文件则上传
        if (fileInfo == null) {
            UploadResult result;
            //上传文件
            //查看缓存，检测文件是否正在上传，没有则创建UploadingFileBO对象，并更新最后上传时间,用于定期清理超时未上传文件
            String fileKey = "uploadingFile_" + BaseContext.getCurrentId() + "_" + transFileId;
            UploadingFileBO o = (UploadingFileBO) redisTemplate.opsForValue().get(fileKey);
            if (o == null) {
                UploadingFileBO uploadingFileBo = new UploadingFileBO(BaseContext.getCurrentId(), transFileId, LocalDateTime.now());
                redisTemplate.opsForValue().set(fileKey, uploadingFileBo);
            } else {
                o.setRecentUpdateTime(LocalDateTime.now());
                redisTemplate.opsForValue().set(fileKey, o);
            }
            //查看缓存，检测该分片是否已经存在
            String chunkKey = "file_" + BaseContext.getCurrentId() + "_" + transFileId + "_" + chunkIndex;
            if (redisTemplate.opsForValue().get(chunkKey) != null) {
                log.info("分片已经存在");
                return;
            }
            //不存在则继续上传
            try {
                result = UploadUtil.upload(transFileId, FileStorageLocationConstant.FILE_PATH, file, chunkIndex, chunks);
                //redis记录已上传分片
                redisTemplate.opsForValue().set(chunkKey, true);
            } catch (Exception e) {
                throw new FileUploadErrorException(MessageConstant.UPLOAD_ERROR);
            }
            //文件还没上传完毕,返回
            if (result.getCode().equals(0)) {
                return;
            }
            //文件上传完毕,清理缓存
            Set keys = redisTemplate.keys("file_" + BaseContext.getCurrentId() + "_" + transFileId);
            redisTemplate.delete(keys);
            // 文件信息表添加文件
            fileInfo = FileInfo.builder()
                    .fileName(result.getFileName())//UUID
                    .fileSize(result.getFileSize())
                    .fileUrl(result.getFileUrl())
                    .pointerNumber(1)
                    .hashValue(fileMD5)
                    .build();
            fileInfoMapper.insertAndGetId(fileInfo);
        } else {
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
        for (UserFile file : userFiles) {
            //哪怕有一个文件不是你的, 所有文件你都别想下载
            if (!file.getUserId().equals(BaseContext.getCurrentId())) {
                throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
            }
            paths.add(file.getFileUrl());
        }
        return paths;
    }
}
