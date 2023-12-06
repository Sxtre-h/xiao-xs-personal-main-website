package com.sxtreh.netdisk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.bo.SharedFileBO;
import com.sxtreh.bo.UploadingFileBO;
import com.sxtreh.bo.UserNetDiskSpaceBo;
import com.sxtreh.constant.*;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.UserFileDTO;
import com.sxtreh.entity.FileInfo;
import com.sxtreh.entity.UserFile;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.exception.FileUploadErrorException;
import com.sxtreh.exception.NetDiskSpaceNotEnoughException;
import com.sxtreh.exception.ParameterErrorException;
import com.sxtreh.netdisk.constant.FileStorageLocationConstant;
import com.sxtreh.netdisk.mapper.FileInfoMapper;
import com.sxtreh.netdisk.mapper.NetDiskMapper;
import com.sxtreh.netdisk.service.NetDiskService;
import com.sxtreh.result.UploadResult;

import com.sxtreh.user.client.UserInfoClient;
import com.sxtreh.utils.SecurityCodeCreator;
import com.sxtreh.utils.UploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NetDiskServiceImpl implements NetDiskService {
    @Autowired
    private NetDiskMapper netDiskMapper;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInfoClient userInfoClient;

    //注意，如果队列不存在，发送的消息会丢失
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FileStorageLocationConstant fileStorageLocationConstant;

    @Override
    public Long initialNetDiskRootCatalog(UserFile userFile) {
        netDiskMapper.insertAndGetId(userFile);
        return userFile.getId();
    }

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

    @Transactional(rollbackFor = Exception.class)
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
//            userInfoClient.modifyUserNetDiskSpace(BaseContext.getCurrentId(), userFile.getFileSize(), ModifyTypeConstant.INCREASE);
            rabbitTemplate.convertAndSend(RabbitMqQueueConstant.USER_NET_DISK_SPACE_QUEUE, new UserNetDiskSpaceBo(BaseContext.getCurrentId(), userFile.getFileSize(), ModifyTypeConstant.INCREASE));
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadFile(MultipartFile file, String fileOriginName, Long transFileId, String fileMD5, Long catalogId, Integer chunkIndex, Integer chunks) {
        //判定父目录合法性
        UserFile fileParent = netDiskMapper.selectById(catalogId);
        if (fileParent == null
                || fileParent.getUserId() != BaseContext.getCurrentId()
                || !fileParent.getFileType().equals(FileTypeConstant.CATALOG)) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //上传第一片时判断用户剩余空间是否充足
        if(chunkIndex == 0){
            Long userRemainSpace = userInfoClient.getUserRemainSpaceInfo(BaseContext.getCurrentId());
            if (userRemainSpace < file.getSize() * chunks) {//注意这里file是分片大小
                throw new NetDiskSpaceNotEnoughException(MessageConstant.NetDiskSpaceNotEnoughException);
            }
        }
        //验证MD5
        FileInfo fileInfo = fileInfoMapper.selectByMD5(fileMD5);
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
                result = UploadUtil.upload(transFileId, fileStorageLocationConstant.FILE_PATH, file, chunkIndex, chunks, fileStorageLocationConstant.SPLIT, fileStorageLocationConstant.TEMP_PATH);
                //redis记录已上传分片
                redisTemplate.opsForValue().set(chunkKey, true);
            } catch (Exception e) {
                throw new FileUploadErrorException(MessageConstant.UPLOAD_ERROR);
            }
            //文件还没上传完毕,返回
            if (result.getCode().equals(0)) {
                return;
            }
            //文件上传完毕,清理正在上传的文件和分片
            Set chunkKeys = redisTemplate.keys("file_" + BaseContext.getCurrentId() + "_" + transFileId + "*");
            redisTemplate.delete(chunkKeys);
            Set fileKeys = redisTemplate.keys("uploadingFile_" + BaseContext.getCurrentId() + "_" + transFileId);
            redisTemplate.delete(fileKeys);
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
            //最后一片
            if (chunkIndex == chunks - 1) {
                //已经存在相同文件则更新文件信息表中文件的引用值
                fileInfoMapper.increasePointNumber(fileInfo.getId());
            }
        }
        //最后一片
        if (chunkIndex == chunks - 1) {
            //用户文件表中添加文件索引
            UserFile userFile = UserFile.builder()
                    .userId(BaseContext.getCurrentId())
                    .fileId(fileInfo.getId())
                    .filePid(catalogId)
                    .fileName(fileOriginName)
                    .fileSize(fileInfo.getFileSize())
                    .fileType(FileTypeConstant.FILE)
                    .fileUrl(fileInfo.getFileUrl())
                    .build();
            netDiskMapper.insert(userFile);
            //更新用户剩余网盘空间
//            userInfoClient.modifyUserNetDiskSpace(BaseContext.getCurrentId(), fileInfo.getFileSize(), ModifyTypeConstant.DECREASE);
            rabbitTemplate.convertAndSend(RabbitMqQueueConstant.USER_NET_DISK_SPACE_QUEUE, new UserNetDiskSpaceBo(BaseContext.getCurrentId(), fileInfo.getFileSize(), ModifyTypeConstant.DECREASE));
        }
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

    @Override
    public String shareFiles(Long fileId) {
        //校验分享用户是否有该文件
        if (netDiskMapper.selectById(fileId).getUserId() != BaseContext.getCurrentId()) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        SharedFileBO sharedFileBO = new SharedFileBO(fileId, BaseContext.getCurrentId());
        String shareCode = SecurityCodeCreator.getCode(8);
        redisTemplate.opsForValue().set(shareCode, sharedFileBO);
        redisTemplate.expire(shareCode, TimeConstant.sharedCodeExpireTime, TimeUnit.DAYS);
        return shareCode;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getSharedFiles(UserFileDTO userFileDTO) {
        //获取分享可以突破空间限制
        //判定父目录合法性
        UserFile fileParent = netDiskMapper.selectById(userFileDTO.getFilePid());
        if (fileParent == null
                || fileParent.getUserId() != BaseContext.getCurrentId()
                || !fileParent.getFileType().equals(FileTypeConstant.CATALOG)) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        SharedFileBO sharedFileBO = (SharedFileBO) redisTemplate.opsForValue().get(userFileDTO.getShareCode());
        if (sharedFileBO != null && sharedFileBO.getShareUserId() != null && sharedFileBO.getFileId() != null) {
            if (!netDiskMapper.selectById(sharedFileBO.getFileId()).getUserId().equals(sharedFileBO.getShareUserId())) {
                throw new ParameterErrorException(MessageConstant.SHARED_CODE_ERROR);
            }
            this.saveFiles(sharedFileBO.getFileId(), userFileDTO.getFilePid());
        }
    }

    //保存一个目录中所有文件到用户网盘
    private void saveFiles(Long fileId, Long filePid) {
        UserFile sharedFile = netDiskMapper.selectById(fileId);
        if (sharedFile == null) return;
        //创建文件并保存
        UserFile myFile = UserFile.builder()
                .userId(BaseContext.getCurrentId())
                .fileId(sharedFile.getFileId())
                .filePid(filePid)
                .fileName(sharedFile.getFileName())
                .fileSize(sharedFile.getFileSize())
                .fileType(sharedFile.getFileType())
                .fileUrl(sharedFile.getFileUrl())
                .build();
        netDiskMapper.insertAndGetId(myFile);
        //文件类型则更新文件信息表引用指针
        if (sharedFile.getFileType().equals(FileTypeConstant.FILE)) {
            Long userRemainSpace = userInfoClient.getUserRemainSpaceInfo(BaseContext.getCurrentId());
            if (userRemainSpace < myFile.getFileSize()) {
                throw new NetDiskSpaceNotEnoughException(MessageConstant.NetDiskSpaceNotEnoughException);
            }
            fileInfoMapper.increasePointNumber(myFile.getFileId());
//            userInfoClient.modifyUserNetDiskSpace(BaseContext.getCurrentId(), myFile.getFileSize(), ModifyTypeConstant.DECREASE);
            rabbitTemplate.convertAndSend(RabbitMqQueueConstant.USER_NET_DISK_SPACE_QUEUE, new UserNetDiskSpaceBo(BaseContext.getCurrentId(), myFile.getFileSize(), ModifyTypeConstant.DECREASE));
        }
        //目录类型则递归添加文件
        else {
            //复制目录下所有文件
            LambdaQueryWrapper<UserFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserFile::getFilePid, sharedFile.getId());
            List<UserFile> sharedFiles = netDiskMapper.selectList(queryWrapper);
            for (UserFile file : sharedFiles) {
                //将每个子文件保存到自己的目录下
                saveFiles(file.getId(), myFile.getId());
            }
        }

    }
}
