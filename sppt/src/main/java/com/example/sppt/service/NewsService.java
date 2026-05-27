package com.example.sppt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.News;

import java.util.List;
import java.util.Map;

public interface NewsService extends IService<News> {

    Map<String, List<News>> homeList(int policyNum, int noticeNum);

    List<News> getPolicyListTopN(int n);

    List<News> getNoticeListTopN(int n);

    List<News> getPolicyList();

    List<News> getNoticeList();

    // 补上分页方法
    IPage<News> pageByType(String type, int pageNum, int pageSize);
}