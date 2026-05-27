package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.News;
import com.example.sppt.mapper.NewsMapper;
import com.example.sppt.service.NewsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
        implements NewsService {

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
}