package com.example.sppt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sppt.entity.SysBannedWord;
import com.example.sppt.mapper.SysBannedWordMapper;
import com.example.sppt.service.SysBannedWordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 禁用词条 Service 实现。
 * findHit 把多段文本拼接后逐词匹配，命中即返回该禁用词，供发布内容前置校验。
 * @author sjy
 * @since 2026-05-30
 */
@Service
public class SysBannedWordServiceImpl extends ServiceImpl<SysBannedWordMapper, SysBannedWord>
        implements SysBannedWordService {

    @Override
    public List<SysBannedWord> listAll() {
        return list();
    }

    @Override
    public String findHit(String... texts) {
        if (texts == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String t : texts) {
            if (t != null) sb.append(t).append('\n');
        }
        String content = sb.toString();
        if (content.isBlank()) return null;

        for (SysBannedWord bw : list()) {
            String word = bw.getWord();
            if (word != null && !word.isBlank() && content.contains(word)) {
                return word;
            }
        }
        return null;
    }
}
