package com.sxtreh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.sxtreh.netdisk.mapper")
@EnableFeignClients
@SpringBootApplication
public class NetDiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetDiskApplication.class, args);
    }
}