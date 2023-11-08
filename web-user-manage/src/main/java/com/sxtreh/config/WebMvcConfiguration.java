package com.sxtreh.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {


    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docketAdmin() {
        log.info("自动配置Api文档");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Web接口文档")
                .version("2.0")
                .description("Web项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sxtreh.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
    @Bean
    public Docket docketUser() {
        log.info("自动配置Api文档");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Web接口文档")
                .version("2.0")
                .description("Web项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sxtreh.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    //设置静态资源映射
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("配置静态资源");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    //拓展消息转化器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息对象转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //为消息转换器设置一个对象转换器，对象转换可以将Java对象序列化为json数据
//        converter.setObjectMapper(new JacksonObjectMapper());
        //将转换器交给容器,index 0 表示将自定义转换器排名放前面, 防止不生效
        converters.add(0, converter);
    }
}
