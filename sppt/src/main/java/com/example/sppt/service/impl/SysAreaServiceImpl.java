package com.example.sppt.service.impl;

/**
 * @author sjy
 * @since 2026-05-28
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysArea;
import com.example.sppt.mapper.SysAreaMapper;
import com.example.sppt.service.SysAreaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
public class SysAreaServiceImpl extends ServiceImpl<SysAreaMapper, SysArea> implements SysAreaService {

    @Override
    public List<Integer> listSelfAndDescendantIds(Integer areaId) {
        // 传 null 或 0：表示“不限区域 / 全省全部”，返回 null，调用方不再加区域条件
        if (areaId == null || areaId == 0) {
            return null;
        }

        // 一次性把整张行政区划表查出来，在内存里按 parentId 做广度优先遍历，
        // 避免对树的每一层都发一次 SQL。数据量（省/市/区/街道）很小，完全够用。
        List<SysArea> all = list();

        // parentId -> 该父级下的直接子节点列表
        Map<Long, List<SysArea>> childrenByParent = all.stream()
                .filter(a -> a.getParentId() != null)
                .collect(Collectors.groupingBy(SysArea::getParentId));

        List<Integer> result = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>();

        // 先放入自身
        result.add(areaId);
        queue.add(areaId.longValue());

        // 逐层向下收集所有子孙
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            List<SysArea> children = childrenByParent.get(cur);
            if (children != null) {
                for (SysArea child : children) {
                    result.add(child.getId().intValue());
                    queue.add(child.getId());
                }
            }
        }
        return result;
    }
}
