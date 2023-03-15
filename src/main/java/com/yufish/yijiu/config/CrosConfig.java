package com.yufish.yijiu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrosConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //**匹配的是我们所有后台的路径，代表后台共享了什么资源
                .allowedOrigins("http://localhost:8082") //匹配的前台的服务器地址
                .maxAge(300 * 1000)
                .allowedHeaders("*")
                .allowedMethods("*"); //允许的前台的请求方式

    }
}