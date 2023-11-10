package com.sxtreh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sxtreh.jwt")
@Data
public class JwtProperties {
    /**
     * 用户生成jwt令牌相关配置
     * yml文件中定义值
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}