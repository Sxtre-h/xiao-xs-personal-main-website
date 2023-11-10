package com.sxtreh;

import com.sxtreh.entity.User;
import com.sxtreh.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class MPTest {
    @Autowired
    private UserMapper userMapper;
    @Test
    public void t1(){
        User user = User.builder()
                        .userName("aaaaaa")
                                .loginName("sddawd")
                                        .password("dawdawdawd")
                                                .build();
        userMapper.insert(user);
    }
}
