package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sppt.entity.News;
import com.example.sppt.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // 1. 首页：政策+公告各取几条（统一用这个，删掉前面重复的）
    @GetMapping("/homeList")
    public Map<String, List<News>> homeList(
            @RequestParam(defaultValue = "5") Integer policyNum,
            @RequestParam(defaultValue = "5") Integer noticeNum
    ) {
        return newsService.homeList(policyNum, noticeNum);
    }

    // 2. 政策页：全部政策
    @GetMapping("/policyList")
    public List<News> policyList() {
        return newsService.getPolicyList();
    }

    // 3. 公告页：全部公告
    @GetMapping("/noticeList")
    public List<News> noticeList() {
        return newsService.getNoticeList();
    }

    // 4. 按 type 分页（政策/公告列表）
    @GetMapping("/list")
    public IPage<News> list(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return newsService.pageByType(type, pageNum, pageSize);
    }

    // 5. 根据ID查询详情
    @GetMapping("/detail")
    public News detail(@RequestParam Integer id) {
        return newsService.getById(id);
    }

    // 6. 查询所有（测试用）
    @GetMapping
    public List<News> listAll() {
        return newsService.list();
    }

    // 7. 路径参数查单个
    @GetMapping("/{id}")
    public News getById(@PathVariable Integer id) {
        return newsService.getById(id);
    }

    // 8. 新增
    @PostMapping
    public boolean save(@RequestBody News news) {
        return newsService.save(news);
    }

    // 9. 修改
    @PutMapping
    public boolean update(@RequestBody News news) {
        return newsService.updateById(news);
    }

    // 10. 删除
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Integer id) {
        return newsService.removeById(id);
    }

    // ================= 首页按城市查询 =================
    @GetMapping("/homeListByCity")
    public Map<String, List<News>> homeListByCity(@RequestParam String city) {
        return newsService.homeListByAreaId(city);
    }

    // ================= 政策按城市查询 =================
    @GetMapping("/policyListByCity")
    public List<News> policyListByCity(@RequestParam String city) {
        if ("all".equals(city)) {
            return newsService.getPolicyList();
        }
        Integer areaId = switch (city) {
            case "taiyuan" -> 1;
            case "lvliang" -> 2;
            case "jinzhong" -> 3;
            default -> null;
        };

        var query = newsService.lambdaQuery()
                .eq(News::getType, "policy");

        if (areaId != null) {
            query.eq(News::getAreaId, areaId);
        }

        return query.list();
    }

    // ================= 公告按城市查询 =================
    @GetMapping("/noticeListByCity")
    public List<News> noticeListByCity(@RequestParam String city) {
        if ("all".equals(city)) {
            return newsService.getNoticeList();
        }
        Integer areaId = switch (city) {
            case "taiyuan" -> 1;
            case "lvliang" -> 2;
            case "jinzhong" -> 3;
            default -> null;
        };

        var query = newsService.lambdaQuery()
                .eq(News::getType, "notice");

        if (areaId != null) {
            query.eq(News::getAreaId, areaId);
        }

        return query.list();
    }

}