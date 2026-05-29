package com.example.sppt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("apply_form")
public class ApplyForm {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private String applyType;
    private Long userId;
    private String contactPhone;
    private Long houseId;
    private String reason;
    private String status;
    private LocalDateTime createTime;

    // 新增：areaId 字段，和前端对应
    private Long areaId;
}