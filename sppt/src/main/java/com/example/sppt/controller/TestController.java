/**
 * @author sjy
 * @since 2026-05-27
 */
package com.example.sppt.controller;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 测试接口：浏览器访问就能看到文字
    @GetMapping("/test")
    public String test(){
        return "后端服务正常！可以连接前端啦！";
    }
}
