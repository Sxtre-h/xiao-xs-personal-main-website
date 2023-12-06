package com.sxtreh.user.controller;

import com.sxtreh.bo.UserNetDiskSpaceBo;
import com.sxtreh.constant.JwtClaimsConstant;
import com.sxtreh.constant.RabbitMqQueueConstant;
import com.sxtreh.dto.UserDTO;
import com.sxtreh.entity.User;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.properties.JwtProperties;
import com.sxtreh.result.Result;
import com.sxtreh.user.annotation.ParameterCheck;
import com.sxtreh.user.service.UserService;
import com.sxtreh.utils.JwtUtil;
import com.sxtreh.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制
 * 注册、登录、登出、查询信息、接收通知、申请邀请码
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 获取用户网盘剩余空间信息，仅限微服务调用
     * @param userId
     * @return
     */
    @GetMapping("info/{userId}")
    Long getUserRemainSpaceInfo(@PathVariable Long userId) {
        return userService.getUserRemainSpaceInfo(userId);
    }

    /**
     * 修改用户网盘剩余空间,数据可靠性要求没那么高，采用异步方式，
     *
     * @param userNetDiskSpaceBo
     */
    @RabbitListener(queuesToDeclare = @Queue(RabbitMqQueueConstant.USER_NET_DISK_SPACE_QUEUE))
    public void modifyUserNetDiskSpace(UserNetDiskSpaceBo userNetDiskSpaceBo) {//序列化对象不需要Object接收后转换，直接接收就行
        userService.modifyUserNetDiskSpace(userNetDiskSpaceBo.getUserId(), userNetDiskSpaceBo.getFileSize(), userNetDiskSpaceBo.getModifyType());
    }
//    改用MQ，弃用
//    @PostMapping("netdisk/spaces")
//    public void modifyUserNetDiskSpace(@RequestParam Long userId, @RequestParam Long increaseSpace, @RequestParam String type) {
//        userService.modifyUserNetDiskSpace(userId, increaseSpace, type);
//    }

    /**
     * 用户申请邀请码
     *
     * @return
     */
    @GetMapping("/invites")
    public Result<UserVO> applyRegisterCode() {
        UserVO userVO = UserVO.builder()
                .registerCode(userService.applyRegisterCode())
                .build();
        return Result.success(userVO);
    }

    /**
     * 用户注册
     *
     * @param userDTO
     * @return
     */

    @ParameterCheck(rule = ParameterRuleType.USER_REGISTER)
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserDTO userDTO) {
        userService.insertUser(userDTO);
        return Result.success();
    }

    /**
     * 用户登录
     *
     * @param userDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.USER_LOGIN)
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserDTO userDTO) {
        User user = userService.login(userDTO);

        //登录成功，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        //返回账号基本信息:头像、用户名、简介、网盘信息
        UserVO userVO = UserVO.builder()
                .userName(user.getUserName())
                .profile(user.getProfile())
                .avatar(user.getAvatar())
                .userNetDiskRootId(user.getUserNetDiskRootId())
                .userSpaceRemain(user.getUserSpaceRemain())
                .userSpaceTotal(user.getUserSpaceTotal())
                .token(token)
                .build();

        return Result.success(userVO);
    }

    /**
     * 用户登出
     * 前端删除token，后端不做处理
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<UserVO> logout() {
        return Result.success();
    }

    /**
     * 查找当前用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public Result<UserVO> info() {
        User user = userService.info();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        //不要返回密码
        userVO.setPassword(null);
        return Result.success(userVO);
    }

    /**
     * 修改用户信息
     *
     * @param userDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.USER_MODIFY)
    @PutMapping("/update")
    public Result<UserVO> updateUser(@RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return Result.success();
    }
}
