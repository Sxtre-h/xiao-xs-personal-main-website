package com.sxtreh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sxtreh.jwt")
//@RefreshScope 热更新配置，但只在本模块有效，有上面那条语句也够了。这条就没意义
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