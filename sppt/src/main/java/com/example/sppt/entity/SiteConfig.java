package com.example.sppt.entity;

/**
 * 站点配置表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_config")
public class SiteConfig {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private Long areaId;        // 区域ID 0总站
    private String siteName;    // 站点名称
    private String logo;        // 站点LOGO
    private String contactInfo; // 联系信息
    private String footerInfo;  // 页脚信息
    private LocalDateTime updateTime; // 更新时间
}