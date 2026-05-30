package com.example.sppt.controller;

import com.example.sppt.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sjy
 * @since 2026-05-27
 */
@RestController
public class TestController {

    // 测试接口：浏览器访问就能看到文字
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端服务正常！可以连接前端啦！");
    }
}
