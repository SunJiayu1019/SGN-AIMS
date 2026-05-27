package com.example.sppt.entity;

/**
 * 禁用词条表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_banned_word")
public class SysBannedWord {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private String word;        // 禁用词
    private LocalDateTime createTime; // 创建时间
}