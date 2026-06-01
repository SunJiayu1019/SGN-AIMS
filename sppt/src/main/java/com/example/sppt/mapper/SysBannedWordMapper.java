package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.SysBannedWord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 禁用词条 Mapper
 * @author sjy
 * @since 2026-05-30
 */
@Mapper
public interface SysBannedWordMapper extends BaseMapper<SysBannedWord> {
}
