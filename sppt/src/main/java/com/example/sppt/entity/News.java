package com.example.sppt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("portal_news")  // 对应数据库表名
public class News {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    private String type;  // policy（政策）/ notice（公告）

    @TableField("area_id")
    private Integer areaId;

    // 发布机构：由表单填写，以「字符串」形式存储（原 publisher_id 字段语义变更）
    @TableField("publish_institution")
    private String publishInstitution;

    // 发布人ID：提交时取「当前登录用户」的 id，以 int 形式存储
    @TableField("publish_id")
    private Integer publishId;

    @TableField("create_time")
    private LocalDateTime createTime;
}