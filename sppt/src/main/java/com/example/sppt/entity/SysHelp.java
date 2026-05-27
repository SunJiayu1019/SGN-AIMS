package com.example.sppt.entity;

/**
 * 帮助文档
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_help")
public class SysHelp {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private String title;       // 帮助标题
    private String content;     // 帮助内容
    private Integer sort;       // 排序
    private LocalDateTime createTime; // 创建时间
}