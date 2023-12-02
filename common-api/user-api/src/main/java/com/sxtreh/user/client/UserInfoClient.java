package com.sxtreh.user.client;

import com.sxtreh.result.Result;
import com.sxtreh.user.entity.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-info-client")
public interface UserInfoClient {

    //todo 单独拆分到passport服务
    /**
     * 注册
     * @param userDTO userDTO
     * @return 用户信息
     */
    @PostMapping("register")
    Result<UserDTO> register(@RequestBody UserDTO userDTO);

    //todo 单独拆分到passport服务

    /**
     * 验证登陆信息
     * @param userDTO userDTO
     * @return 用户信息
     */
    @PostMapping("login")
    Result<UserDTO> login(@RequestBody UserDTO userDTO);

    //todo 这个应该直接放到passport
    Result<UserDTO> logout();

    /**
     * 查找当前用户信息
     * @return UserDTO
     */
    @GetMapping("info/{accountId}")
    Result<UserDTO> info(@PathVariable Long accountId);

    @PostMapping("update-user")
    Result<UserDTO> updateUser(@RequestBody UserDTO userDTO);
}
