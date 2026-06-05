package com.example.sppt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.sppt.dto.Result;
import com.example.sppt.entity.News;
import com.example.sppt.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公告 / 政策接口（/news/**）
 * 统一后：
 *   1. 只依赖 NewsService，不再直接依赖 Mapper；
 *   2. 所有接口统一返回 Result<T>；
 *   3. “城市编码 -> 区域ID”等业务逻辑已下沉到 Service，本类不再写 switch。
 * @author sjy
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // 1. 首页：政策+公告各取几条
    @GetMapping("/homeList")
    public Result<Map<String, List<News>>> homeList(
            @RequestParam(defaultValue = "5") Integer policyNum,
            @RequestParam(defaultValue = "5") Integer noticeNum) {
        return Result.success(newsService.homeList(policyNum, noticeNum));
    }

    // 2. 政策页：全部政策
    @GetMapping("/policyList")
    public Result<List<News>> policyList() {
        return Result.success(newsService.getPolicyList());
    }

    // 3. 公告页：全部公告
    @GetMapping("/noticeList")
    public Result<List<News>> noticeList() {
        return Result.success(newsService.getNoticeList());
    }

    // 4. 按 type 分页（政策/公告列表）
    @GetMapping("/list")
    public Result<IPage<News>> list(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(newsService.pageByType(type, pageNum, pageSize));
    }
 
    // 5. 审批网站管理页：按 类型 + 区域 查询（areaId 为空表示不限区域）
    //    路径为两段（/manage/list），不会与 /{id} 冲突。
    @GetMapping("/manage/list")
    public Result<List<News>> manageList(
            @RequestParam String type,
            @RequestParam(required = false) Integer areaId) {
        return Result.success(newsService.listByTypeAndArea(type, areaId));
    }

    // 6. 根据ID查询详情
    @GetMapping("/detail")
    public Result<News> detail(@RequestParam Integer id) {
        return Result.success(newsService.getById(id));
    }

    // 7. 查询所有
    @GetMapping
    public Result<List<News>> listAll() {
        return Result.success(newsService.list());
    }

    // 8. 路径参数查单个
    @GetMapping("/{id}")
    public Result<News> getById(@PathVariable Integer id) {
        return Result.success(newsService.getById(id));
    }

    // 9. 新增（自动补 createTime；禁用词校验失败返回 fail）
    @PostMapping
    public Result<Boolean> save(@RequestBody News news) {
        try {
            return Result.success(newsService.saveNews(news));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 10. 修改（同样做禁用词校验）
    @PutMapping
    public Result<Boolean> update(@RequestBody News news) {
        try {
            return Result.success(newsService.updateNews(news));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 11. 删除
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(newsService.removeById(id));
    }

    // ================= 首页按城市查询 =================
    @GetMapping("/homeListByCity")
    public Result<Map<String, List<News>>> homeListByCity(@RequestParam String city) {
        return Result.success(newsService.homeListByAreaId(city));
    }

    // ================= 政策按城市查询（业务逻辑已收口到 Service）=================
    @GetMapping("/policyListByCity")
    public Result<List<News>> policyListByCity(@RequestParam String city) {
        return Result.success(newsService.getPolicyListByCity(city));
    }

    // ================= 公告按城市查询 =================
    @GetMapping("/noticeListByCity")
    public Result<List<News>> noticeListByCity(@RequestParam String city) {
        return Result.success(newsService.getNoticeListByCity(city));
    }
}
