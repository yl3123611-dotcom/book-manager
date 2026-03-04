package com.book.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 资源映射配置
 * 核心修复：让浏览器访问 /upload/** 时，去 application.yml 配置的 D 盘路径找文件
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 1. 注入 application.yml 中配置的路径 (即 D:/biyesheji/upload/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 2. 规范化路径：如果配置里没有以 "/" 结尾，自动补上
        String path = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";

        // 3. 配置映射关系
        // 浏览器请求: http://localhost:8080/upload/xxx.jpg
        // 实际查找: file:D:/biyesheji/upload/xxx.jpg
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + path);

        // Backward-compatible alias for historical cover paths stored as /images/upload/**
        registry.addResourceHandler("/images/upload/**")
                .addResourceLocations("file:" + path);
    }
}