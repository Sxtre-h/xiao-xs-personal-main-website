package com.sxtreh.netdisk.aspect;

import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.*;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.exception.ParameterErrorException;
import com.sxtreh.netdisk.annotation.ParameterCheck;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 统一管理参数格式以及有效性
 * 拦截非法参数，消除多余参数，避免不合理修改
 */
@Slf4j
@Aspect
@Component
public class ParameterCheckAspect {
    //参数格式匹配
    private static final String registerCodeRegex = "^.{6}$";
    private static final String shareCodeRegex = "^.{8}$";
    private static final String loginNameRegex = "^.{6,20}$";
    private static final String userNameRegex = "^.{1,20}$";
    private static final String passwordRegex = "^.{6,30}$";
    private static final String userProfileRegex = "^.{0,50}$";
    private static final String userAvatarRegex = "^.{0,50}$";
    private static final String fileNameRegex = "^.{1,100}$";
    private static final String noteNameRegex = "^.{1,32}";
    private static final String noteBodyRegex = "^.{0,1024}";
    private static final String catalogNameRegex = "^.{1,30}$";
    private static final String catalogLevelRegex = "^[0-9]$";

    @Pointcut("@annotation(com.sxtreh.netdisk.annotation.ParameterCheck)")
    public void parameterCheckPoint() {
    }

    @Before(value = "parameterCheckPoint() && @annotation(parameterCheck)")
    public void parameterCheck(JoinPoint joinPoint, ParameterCheck parameterCheck) {
        //获取被拦截方法的参数列表
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return;
        //获取最常用的第一个参数, 一般是DTO对象
        Object entity = args[0];
        //获取校验规则类型
        ParameterRuleType rule = parameterCheck.rule();
        //校验参数合法性
        switch (rule) {
            //用户参数校验
            case USER_REGISTER -> {
                UserDTO userDTO = (UserDTO) entity;
                if (userDTO.getUserName() != null && userDTO.getUserName().matches(userNameRegex)
                        && userDTO.getLoginName() != null && userDTO.getLoginName().matches(loginNameRegex)
                        && userDTO.getPassword() != null && userDTO.getPassword().matches(passwordRegex)
                        && (userDTO.getProfile() == null || userDTO.getProfile().matches(userProfileRegex))
                        && (userDTO.getAvatar() == null || userDTO.getAvatar().matches(userAvatarRegex))
                        && (userDTO.getRegisterCode() != null && userDTO.getRegisterCode().matches(registerCodeRegex)))
                    return;
            }
            case USER_LOGIN -> {
                UserDTO userDTO = (UserDTO) entity;
                if (userDTO.getLoginName() != null
                        && userDTO.getLoginName().matches(loginNameRegex)
                        && userDTO.getPassword() != null
                        && userDTO.getPassword().matches(passwordRegex))
                    return;
            }
            case USER_MODIFY -> {
                UserDTO userDTO = (UserDTO) entity;
                if (userDTO.getUserName() != null
                        && userDTO.getUserName().matches(userNameRegex)
                        && (userDTO.getPassword() == null || userDTO.getPassword().matches(passwordRegex))//密码为空表示不更新
                        && (userDTO.getProfile() == null || userDTO.getProfile().matches(userProfileRegex))
                        && (userDTO.getAvatar() == null || userDTO.getAvatar().matches(userAvatarRegex))) {
                    //禁止修改的参数
                    userDTO.setLoginName(null);
                    return;
                }
            }
            //笔记参数校验
            //case NOTE_LIST -> {}//路径参数不需要校验
            case NOTE_SAVE -> {
                NoteDTO noteDTO = (NoteDTO) entity;
                if (noteDTO.getCatalogId() != null
                        && (noteDTO.getNoteName() != null && noteDTO.getNoteName().matches(noteNameRegex))
                        && (noteDTO.getNoteBody() == null || noteDTO.getNoteBody().toString().matches(noteBodyRegex)))
//                        && (noteDTO.getNoteBody() != null && noteDTO.getNoteBody().toString().matches(noteBodyRegex)))
                    return;
                //没有Note类noteId字段，不会传进去，不用置空
            }
            case NOTE_SAVE_COL -> {
                NoteColDTO noteDTO = (NoteColDTO) entity;
                if (noteDTO.getCatalogId() != null)
                    return;
            }
            case NOTE_MODIFY -> {
                NoteDTO noteDTO = (NoteDTO) entity;
                if (noteDTO.getNoteId() != null
                        && (noteDTO.getNoteName() != null && noteDTO.getNoteName().matches(noteNameRegex))
                        && (noteDTO.getNoteBody() == null || noteDTO.getNoteBody().toString().matches(noteBodyRegex))) {
//                        && (noteDTO.getNoteBody() != null && noteDTO.getNoteBody().toString().matches(noteBodyRegex))) {
                    //暂时不支持移动笔记位置
                    noteDTO.setCatalogId(null);
                    return;
                }
            }
            case NOTE_COLS_MODIFY -> {
                //要求所有对象都不为空
                for (Object arg : args) {
                    if (arg == null) throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
                }
                return;
            }
            case NOTE_DELETE -> {
                NoteDTO noteDTO = (NoteDTO) entity;
                if (noteDTO.getNoteId() != null) return;
            }
            case NOTE_DELETE_COL -> {
                NoteColDTO noteColDTO = (NoteColDTO) entity;
                if (noteColDTO.getCatalogId() != null && noteColDTO.getNoteColName().matches(noteBodyRegex)) {
                    return;
                }
            }
            //case NOTE_CATALOG_LIST -> {}
            case NOTE_CATALOG_SAVE -> {
                NoteCatalogDTO noteCatalogDTO = (NoteCatalogDTO) entity;
                if (noteCatalogDTO.getCatalogLevel() != null
                        && (noteCatalogDTO.getCatalogName() != null && noteCatalogDTO.getCatalogName().matches(catalogNameRegex))
                        && (noteCatalogDTO.getCatalogLevel().equals(0) || noteCatalogDTO.getParentCatalogId() != null)) //目录非0级目录且没有指定父目录
                    return;
            }
            case NOTE_CATALOG_MODIFY -> {
                NoteCatalogDTO noteCatalogDTO = (NoteCatalogDTO) entity;
                if ((noteCatalogDTO.getCatalogName() != null && noteCatalogDTO.getCatalogName().matches(catalogNameRegex))
                        && noteCatalogDTO.getCatalogId() != null) {
                    //暂时不支持移动目录
                    noteCatalogDTO.setParentCatalogId(null);
                    noteCatalogDTO.setCatalogLevel(null);
                    return;
                }
            }
            case NOTE_CATALOG_DELETE -> {
                NoteCatalogDTO noteCatalogDTO = (NoteCatalogDTO) entity;
                if (noteCatalogDTO.getCatalogId() != null) return;
            }
            //网盘参数校验
            case NET_DISK_CATALOG_SAVE -> {
                UserFileDTO userFileDTO = (UserFileDTO) entity;
                if (userFileDTO.getFilePid() != null
                        && (userFileDTO.getFileName() != null && userFileDTO.getFileName().matches(fileNameRegex))) {
                    userFileDTO.setFileId(null);
//                    userFileDTO.setFileSize(null);
//                    userFileDTO.setFileType(null);
                    return;
                }
            }
            case NET_DISK_FILE_DELETE -> {
                UserFileDTO userFileDTO = (UserFileDTO) entity;
                if (userFileDTO.getFileId() != null) return;
            }
            case NET_DISK_FILE_MODIFY -> {
                UserFileDTO userFileDTO = (UserFileDTO) entity;
                if (userFileDTO.getFileId() != null
                        && (userFileDTO.getFileName() == null || userFileDTO.getFileName().matches(fileNameRegex))) {
                    return;
                }
            }
            case NET_DISK_FILE_SHARE -> {
                UserFileDTO userFileDTO = (UserFileDTO) entity;
                if (userFileDTO.getFileId() != null) {
                    return;
                }
            }
            case NET_DISK_GET_SHARED_FILES -> {
                UserFileDTO userFileDTO = (UserFileDTO) entity;
                if (userFileDTO.getShareCode() != null && userFileDTO.getShareCode().matches(shareCodeRegex)
                        && userFileDTO.getFilePid() != null) {
                    return;
                }
            }
            case ALL_NOT_NULL, NET_DISK_FILE_UPLOAD, NET_DISK_FILE_LIST, NET_DISK_FILE_DOWNLOAD -> {
                //要求所有对象都不为空
                for (Object arg : args) {
                    if (arg == null) throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
                }
                return;
            }
        }
        //存在参数缺失或者格式不正确
        throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
    }
}
