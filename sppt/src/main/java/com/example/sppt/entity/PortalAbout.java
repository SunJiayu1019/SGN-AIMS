package com.example.sppt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("portal_about")
public class PortalAbout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String areaId;
    private String introduction;
    private String contactInfo;
}