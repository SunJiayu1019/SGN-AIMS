package com.example.sppt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 静态资源映射：把磁盘上的上传目录 file.upload-dir 暴露为 /uploads/** 访问路径。
 *
 * 例：磁盘 ./uploads/news/2026-06-01/x.png
 *     浏览器 http://localhost:8080/uploads/news/2026-06-01/x.png
 *
 * 这样上传的图片无需经过额外接口，直接当静态资源访问。
 * @author sjy
 * @since 2026-06-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 关键：addResourceLocations 的路径**必须以 / 结尾**，否则 Spring 会把
        // 最后一段当成文件名前缀，导致 /uploads/** 永远匹配不到文件（图片 404）。
        String abs = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        // 统一成正斜杠，并补上结尾的 /
        String normalized = abs.replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        String location = "file:" + normalized;   // 形如 file:/D:/project/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
