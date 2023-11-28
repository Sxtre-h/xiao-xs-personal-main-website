package com.sxtreh.user.controller;

import com.sxtreh.result.Result;
import com.sxtreh.user.client.UserInfoClient;
import com.sxtreh.user.entity.dto.UserDTO;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController implements UserInfoClient {

    //todo 实现逻辑保持原样

    @Override
    public Result<UserDTO> register(UserDTO userDTO) {
        return null;
    }

    @Override
    public Result<UserDTO> login(UserDTO userDTO) {
        return null;
    }

    @Override
    public Result<UserDTO> logout() {
        return null;
    }

    @Override
    public Result<UserDTO> info(Long accountId) {
        return null;
    }

    @Override
    public Result<UserDTO> updateUser(UserDTO userDTO) {
        return null;
    }
}
