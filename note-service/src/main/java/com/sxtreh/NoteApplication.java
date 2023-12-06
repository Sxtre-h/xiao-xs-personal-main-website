package com.sxtreh;

import com.sxtreh.netdisk.client.NetDiskClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = {NetDiskClient.class}, clients = NetDiskClient.class)
@MapperScan("com.sxtreh.note.mapper")
@SpringBootApplication
public class NoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(NoteApplication.class, args);
    }
}