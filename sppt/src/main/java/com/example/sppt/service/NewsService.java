package com.example.sppt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sppt.entity.News;

import java.util.List;
import java.util.Map;

/**
 * 公告 / 政策 Service
 * 统一约定：所有“政策/公告”相关的业务规则（含 城市编码 -> 区域ID 的映射、
 *           按区域过滤等）都收口在本 Service，Controller 不再写业务逻辑。
 * @author sjy
 */
public interface NewsService extends IService<News> {

    Map<String, List<News>> homeList(int policyNum, int noticeNum);

    List<News> getPolicyListTopN(int n);

    List<News> getNoticeListTopN(int n);

    List<News> getPolicyList();

    List<News> getNoticeList();

    // 按 type 分页（政策/公告列表）
    IPage<News> pageByType(String type, int pageNum, int pageSize);

    // 首页：按城市编码获取政策 + 公告
    Map<String, List<News>> homeListByAreaId(String city);

    // 政策：按城市编码获取（原先写在 Controller 里的 switch 已收口到这里）
    List<News> getPolicyListByCity(String city);

    // 公告：按城市编码获取
    List<News> getNoticeListByCity(String city);

    // 审批网站管理页：按 类型 + 区域 查询（areaId 为空表示不限区域）
    List<News> listByTypeAndArea(String type, Integer areaId);

    // 新增公告/政策：自动补 createTime 后入库
    boolean saveNews(News news);

    // 修改公告/政策：同样做禁用词校验
    boolean updateNews(News news);
}
