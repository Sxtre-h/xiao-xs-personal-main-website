package com.sxtreh;

import com.sxtreh.netdisk.client.NetDiskClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

//@EnableFeignClients(basePackageClasses = NetDiskClient.class)
@EnableFeignClients(basePackageClasses = {NetDiskClient.class}, clients = NetDiskClient.class)
@MapperScan("com.sxtreh.user.mapper")
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

//    @Bean
//    public IRule randomRule(){
//        return new RandomRule();
//    }
}