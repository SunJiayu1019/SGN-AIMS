package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.News;
import com.example.sppt.mapper.NewsMapper;
import com.example.sppt.service.NewsService;
import com.example.sppt.service.SysAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
        implements NewsService {

    private final SysAreaService sysAreaService;

    /**
     * 兼容老的“城市编码”入参（taiyuan/lvliang/jinzhong/all）。
     * 现在统一改为查 sys_area 表里 code 对应的区域 id，而不是写死的 1/2/3，
     * 这样新增了别的市/区也不用改代码。
     * 返回 null 表示“不限区域”。
     */
    private Integer cityToAreaId(String city) {
        if (city == null || "all".equals(city) || city.isBlank()) {
            return null;
        }
        // 约定 sys_area.code 用城市拼音（taiyuan/lvliang/jinzhong…）；
        // 若你的 code 是行政区划数字编码，把这里换成按 name 查即可。
        var area = sysAreaService.lambdaQuery()
                .eq(com.example.sppt.entity.SysArea::getCode, city)
                .one();
        return area == null ? null : area.getId().intValue();
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
                .orderByDesc(News::getId)
                .last("LIMIT " + n));
    }

    @Override
    public List<News> getNoticeListTopN(int n) {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "notice")
                .orderByDesc(News::getId)
                .last("LIMIT " + n));
    }

    @Override
    public List<News> getPolicyList() {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "policy")
                .orderByDesc(News::getId));
    }

    @Override
    public List<News> getNoticeList() {
        return list(new LambdaQueryWrapper<News>()
                .eq(News::getType, "notice")
                .orderByDesc(News::getId));
    }

    @Override
    public IPage<News> pageByType(String type, int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<News>()
                        .eq(News::getType, type)
                        .orderByDesc(News::getId));
    }

    @Override
    public Map<String, List<News>> homeListByAreaId(String city) {
        Integer areaId = cityToAreaId(city);

        // 关键：按“该区域 + 其所有下级区域”来查，而不是只查这一个 areaId。
        // 例如选中山西省，会把太原/吕梁…以及下属各区县的内容都查出来。
        List<News> policyList = listByTypeAndArea("policy", areaId);
        List<News> noticeList = listByTypeAndArea("notice", areaId);

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
        // 把“某个区域”展开成“它自己 + 全部下级区域”的 id 列表，
        // 实现“点击省看到全省、点击市看到全市”的层级聚合效果。
        List<Integer> areaIds = sysAreaService.listSelfAndDescendantIds(areaId);

        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getType, type);
        if (areaIds != null) {
            // areaId 非空 -> 限定在 自身+子孙 范围内
            wrapper.in(News::getAreaId, areaIds);
        }
        // areaIds == null（传入 null/0）-> 不加区域条件，查全部
        wrapper.orderByDesc(News::getId);
        return list(wrapper);
    }

    @Override
    public boolean saveNews(News news) {
        if (news.getCreateTime() == null) {
            news.setCreateTime(LocalDateTime.now());
        }
        // publisherId / areaId 由前端表单提交，MyBatis-Plus 会按字段原样写入；
        // 这里不再对它们做任何覆盖，确保“发布人ID / 所属子站”都能正确入库。
        return save(news);
    }
}
