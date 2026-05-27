package com.example.sppt.entity;

/**
 * @author sjy
 * @since 2026-05-27
 */

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
    private String applyNo;        // 申请单号
    private String applyType;      // 申请类型 new/reissue
    private Long userId;           // 申请人ID
    private String contactPhone;   // 联系电话
    private Long houseId;          // 门牌ID
    private String reason;         // 申请理由
    private String status;         // 状态 PENDING/APPROVED/REJECTED
    private LocalDateTime createTime;
}