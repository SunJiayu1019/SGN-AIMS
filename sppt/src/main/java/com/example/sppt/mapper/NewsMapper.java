package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 / 政策 Mapper
 * @author sjy
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}
