package com.example.sppt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
//自动生成get/set
@TableName("portal_news")//绑定数据库表名
public class News {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;
    private String content;
    private String type;
    private Integer areaId;
    private Integer publisherId;
    private LocalDateTime createTime;
}