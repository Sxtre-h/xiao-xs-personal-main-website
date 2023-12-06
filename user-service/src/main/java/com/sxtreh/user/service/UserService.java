package com.sxtreh.user.service;

import com.sxtreh.dto.UserDTO;
import com.sxtreh.entity.User;

public interface UserService {

     User login(UserDTO userDTO);

     User info();

     void updateUser(UserDTO userDTO);

     void insertUser(UserDTO userDTO);

    String applyRegisterCode();

    Long getUserRemainSpaceInfo(Long userId);

    void modifyUserNetDiskSpace(Long userId, Long increaseSpace, String type);
}
