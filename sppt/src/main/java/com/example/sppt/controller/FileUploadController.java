package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件上传接口（/api/upload/**）。
 *
 * 用途：审批网站管理上传政策/公告配图。
 *   - 图片文件保存到磁盘目录 file.upload-dir（默认 ./uploads）；
 *   - 返回「访问相对路径」如 /uploads/news/2026-06-01/xxx.png，
 *     前端把它存进 portal_news.cover_image，展示时拼成 http://host:8080 + 路径；
 *   - 静态访问由 WebMvcConfig 把 /uploads/** 映射到该磁盘目录。
 *
 * 注意：不要把图片二进制存进数据库；数据库只存路径字符串。
 * @author sjy
 * @since 2026-06-01
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    /** 上传根目录，可在 application.yml 配置 file.upload-dir 覆盖。 */
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 允许的图片后缀。 */
    private static final String[] ALLOW_EXT = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};

    /** 单文件大小上限：5MB。 */
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    /**
     * 上传图片。
     * @param file   表单字段名 file
     * @param module 业务子目录，如 news（默认 common）
     * @return 访问相对路径，例如 /uploads/news/2026-06-01/uuid.png
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      @RequestParam(defaultValue = "common") String module) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.fail("图片超过 5MB 大小限制");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();
        boolean ok = false;
        for (String e : ALLOW_EXT) if (e.equals(ext)) { ok = true; break; }
        if (!ok) {
            return Result.fail("只允许上传图片（jpg/png/gif/webp/bmp）");
        }

        try {
            // 按 模块/日期 分目录，避免单目录文件过多
            String datePath = LocalDate.now().toString(); // 2026-06-01
            Path dir = Paths.get(uploadDir, module, datePath).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = dir.resolve(filename).toFile();
            file.transferTo(dest);

            // 返回对外访问的相对路径（与 WebMvcConfig 的 /uploads/** 映射对应）
            String accessPath = "/uploads/" + module + "/" + datePath + "/" + filename;
            return Result.success(accessPath);
        } catch (IOException e) {
            return Result.fail("保存文件失败：" + e.getMessage());
        }
    }
}
