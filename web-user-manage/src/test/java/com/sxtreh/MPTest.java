package com.sxtreh;

import com.sxtreh.entity.User;
import com.sxtreh.enumeration.FileType;
import com.sxtreh.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;


@Slf4j
//@SpringBootTest
public class MPTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void t1(){
        User user = User.builder()
                        .userName("aaaaaa")
                                .loginName("sddawd")
                                        .password("dawdawdawd")
                                                .build();
        userMapper.insert(user);
    }

    @Test
    public void t2(){
        String key = "11";
        String key2 = "12";
        if(redisTemplate.opsForValue().get(key)!=null)
            log.info("already");
        else {
            redisTemplate.opsForValue().set(key, "11");
            log.info("new");
        }
        if(redisTemplate.opsForValue().get(key2)!=null)
            log.info("already");
        else {
            redisTemplate.opsForValue().set(key2, "1122");
            log.info("new");
        }
    }
    @Test
    public void t3(){
        Set keys = redisTemplate.keys("1*");
        redisTemplate.delete(keys);
    }
}
