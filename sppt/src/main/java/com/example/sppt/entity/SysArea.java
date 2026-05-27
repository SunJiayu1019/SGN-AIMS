package com.example.sppt.entity;

/**
 * 行政区划表（省/市/区/街道，多级）
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_area")
public class SysArea {
    @TableId(type = IdType.AUTO)
    private Long id;            // 行政区划ID
    private String name;        // 区域名称
    private String code;        // 区域编码
    private Integer level;      // 层级 1省 2市 3区/县 4街道
    private Long parentId;      // 上级ID
    private String parentPath;  // 父级路径
    private Integer sort;       // 排序
}