package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.constant.DataSizeConstant;
import com.sxtreh.constant.FileTypeConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.UserDTO;
import com.sxtreh.entity.User;
import com.sxtreh.entity.UserFile;
import com.sxtreh.exception.AccountAlreadyExistException;
import com.sxtreh.exception.AccountBannedException;
import com.sxtreh.exception.AccountNotFoundException;
import com.sxtreh.exception.PasswordErrorException;
import com.sxtreh.mapper.NetDiskMapper;
import com.sxtreh.mapper.UserMapper;
import com.sxtreh.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NetDiskMapper netDiskMapper;

    /**
     * 用户注册
     * @param userDTO
     */
    @Override
    public void insertUser(UserDTO userDTO) {
        if(userMapper.selectByLoginName(userDTO.getLoginName()) != null){
            throw new AccountAlreadyExistException(MessageConstant.ALREADY_EXISTS);
        }
        //创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        //为用户分配网盘空间
        user.setUserNetDiskRootId(0L);//临时分配一个空的目录ID
        user.setUserSpaceRemain(DataSizeConstant._10GB);
        user.setUserSpaceTotal(DataSizeConstant._10GB);
        userMapper.insertAndGetId(user);
        //为用户创建网盘根目录
        UserFile userFile = UserFile.builder()
                .userId(user.getId())
                //将根目录的父目录id设置为用户的id
                .filePid(user.getId())
                .fileName(user.getUserName())
                .fileType(FileTypeConstant.CATALOG)
                .build();
        netDiskMapper.insertAndGetId(userFile);
        //将用户网盘根目录写入用户信息表
        user.setUserNetDiskRootId(userFile.getId());
        userMapper.updateById(user);
    }

    /**
     * 用户登录
     * @param userDTO
     * @return
     */
    @Override
    public User login(UserDTO userDTO){
        String loginName = userDTO.getLoginName();
        String password = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());

        User user = userMapper.selectByLoginName(loginName);

        //账号不存在
        if (user == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //密码错误
        if(!password.equals(user.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        //账号被封禁
        if(user.getStatus() == StatusConstant.BANNED){
            throw new AccountBannedException(MessageConstant.ACCOUNT_BANNED);
        }

        return user;
    }
    /**
     * 查找登录用户信息
     */
    @Override
    public User info() {
        return userMapper.selectById(BaseContext.getCurrentId());
    }

    /**
     * 更新用户信息
     * @param userDTO
     */

    @Override
    public void updateUser(UserDTO userDTO) {

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        //防止没有输入新密码时密码被更改
        if(user.getPassword() != null){
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
//        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
//                .eq("id", BaseContext.getCurrentId());
        //取消硬编码
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getId, BaseContext.getCurrentId());
        userMapper.update(user, queryWrapper);
    }
}
