package com.sxtreh.controller;

import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.constant.JwtClaimsConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.UserDTO;
import com.sxtreh.entity.User;
import com.sxtreh.exception.ParameterMissingException;
import com.sxtreh.properties.JwtProperties;
import com.sxtreh.result.Result;
import com.sxtreh.service.UserService;
import com.sxtreh.utils.JwtUtil;
import com.sxtreh.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制
 * 注册、登录、登出、查询信息、接收通知、申请邀请码
 * TODO 参数格式校验,用正则表达式
 */
@Slf4j
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户注册
     * @param userDTO
     * @return
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserDTO userDTO){
        if(userDTO.getLoginName() == null || userDTO.getUserName() == null || userDTO.getPassword() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        //TODO 邀请码校验
        userService.insertUser(userDTO);
//        this.login(userDTO);
        return Result.success();
    }

    /**
     * 用户登录
     * @param userDTO
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserDTO userDTO){
        if(userDTO.getLoginName() == null || userDTO.getPassword() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        User user = userService.login(userDTO);

        //登录成功，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        //返回账号基本信息:头像、用户名、简介
        UserVO userVO = UserVO.builder()
                .userName(user.getUserName())
                .profile(user.getProfile())
                .avatar(user.getAvatar())
                .token(token)
                .build();

        return Result.success(userVO);
    }

    /**
     * 用户登出
     * 前端删除token，后端不做处理
     * @return
     */
    @RequireLogin
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<UserVO> logout(){
        return Result.success();
    }

    /**
     * 查找当前用户信息
     * @return
     */
    @RequireLogin
    @ApiOperation("查找当前登录用户信息")
    @GetMapping("/info")
    public Result<UserVO> info(){
        User user = userService.info();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        //不要返回密码
        userVO.setPassword(null);
        return Result.success(userVO);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @RequireLogin
    @ApiOperation("修改用户信息")
    @PutMapping("/update")
    public Result<UserVO> updateUser(@RequestBody UserDTO userDTO){
        //User中设置了部分字段即使为空也会更新，所以不需要判断userDTO中是否所有字段都为空
        userService.updateUser(userDTO);
        return Result.success();
    }
}
