package com.example.sppt.entity;

/**
 * 数据字典
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private String dictType;    // 字典类型
    private String dictKey;     // 字典键
    private String dictValue;   // 字典值
    private Integer sort;       // 排序
}