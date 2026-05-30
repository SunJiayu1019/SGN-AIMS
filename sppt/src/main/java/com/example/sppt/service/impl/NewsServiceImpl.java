package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.News;
import com.example.sppt.mapper.NewsMapper;
import com.example.sppt.service.NewsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
        implements NewsService {

    /**
     * 城市编码 -> 区域ID 的唯一映射（原先 Controller 和 Service 各写了一份，现统一在此）。
     * 与后端约定：1=太原 2=吕梁 3=晋中。返回 null 表示“不限区域”。
     */
    private Integer cityToAreaId(String city) {
        if (city == null || "all".equals(city)) {
            return null;
        }
        return switch (city) {
            case "taiyuan" -> 1;
            case "lvliang" -> 2;
            case "jinzhong" -> 3;
            default -> null;
        };
    }

    @Override
    public Map<String, List<News>> homeList(int policyNum, int noticeNum) {
        List<News> policyList = getPolicyListTopN(policyNum);
        List<News> noticeList = getNoticeListTopN(noticeNum);
        Map<String, List<News>> map = new HashMap<>();
        map.put("policyList", policyList);
        map.put("noticeList", noticeList);
        return map;
    }

    @Override
    public List<News> getPolicyListTopN(int n) {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "policy")
                .last("LIMIT " + n));
    }

    @Override
    public List<News> getNoticeListTopN(int n) {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "notice")
                .last("LIMIT " + n));
    }

    @Override
    public List<News> getPolicyList() {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "policy"));
    }

    @Override
    public List<News> getNoticeList() {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "notice"));
    }

    @Override
    public IPage<News> pageByType(String type, int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<News>().eq(News::getType, type));
    }

    @Override
    public Map<String, List<News>> homeListByAreaId(String city) {
        List<News> policyList;
        List<News> noticeList;

        Integer areaId = cityToAreaId(city);

        if (areaId == null) {
            // 全省：政策 / 公告各取 6 条
            policyList = list(new LambdaQueryWrapper<News>()
                    .eq(News::getType, "policy")
                    .last("LIMIT 6"));
            noticeList = list(new LambdaQueryWrapper<News>()
                    .eq(News::getType, "notice")
                    .last("LIMIT 6"));
        } else {
            // 具体市：显示该市全部
            policyList = list(new LambdaQueryWrapper<News>()
                    .eq(News::getType, "policy")
                    .eq(News::getAreaId, areaId));
            noticeList = list(new LambdaQueryWrapper<News>()
                    .eq(News::getType, "notice")
                    .eq(News::getAreaId, areaId));
        }

        Map<String, List<News>> map = new HashMap<>();
        map.put("policyList", policyList);
        map.put("noticeList", noticeList);
        return map;
    }

    @Override
    public List<News> getPolicyListByCity(String city) {
        return listByTypeAndArea("policy", cityToAreaId(city));
    }

    @Override
    public List<News> getNoticeListByCity(String city) {
        return listByTypeAndArea("notice", cityToAreaId(city));
    }

    @Override
    public List<News> listByTypeAndArea(String type, Integer areaId) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getType, type);
        if (areaId != null) {
            wrapper.eq(News::getAreaId, areaId);
        }
        wrapper.orderByDesc(News::getId);
        return list(wrapper);
    }

    @Override
    public boolean saveNews(News news) {
        if (news.getCreateTime() == null) {
            news.setCreateTime(LocalDateTime.now());
        }
        return save(news);
    }
}
