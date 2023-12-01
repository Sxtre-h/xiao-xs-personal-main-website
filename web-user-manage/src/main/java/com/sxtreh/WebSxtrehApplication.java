package com.sxtreh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@MapperScan("com.sxtreh.mapper")
@SpringBootApplication
public class WebSxtrehApplication {

    public static void main(String[] args) {

        SpringApplication.run(WebSxtrehApplication.class, args);
    }

}
