package com.example.sppt.entity;

/**
 * 门牌申请表
 *
 * 字段与「数据库重制版」对齐：
 *  - applyNo            申请单号 AP202605010001
 *  - applyType          申请类型 new(新申请) / reissue(补发)
 *  - userId             申请人ID
 *  - applicantName      申请人姓名（与数据库约定保留：表中需补回 applicant_name 列）
 *  - contactPhone       联系电话
 *  - originalHouseCode  补发时填写的原门牌号；存在于 house_info.house_code 中。
 *                       新申请时为 NULL。（数据库为 VARCHAR(50)）
 *  - houseType          房屋类型 house/shop/factory（与数据库约定保留：表中需补回 house_type 列）
 *  - detailAddress      房屋详细地址
 *  - reason             申请理由
 *  - status             状态 PENDING/APPROVED/REJECTED
 *  - areaId             申请人所在街道（level=4 街道ID）
 *  - houseId            审批通过后分配的「三位门牌号」（如 7 -> "007"），
 *                       与街道 areaId 拼接成 house_code。新申请通过后回写，补发为空。
 *  - createTime         创建时间
 *
 * @author sjy
 * @since 2026-05-31
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
    private String applyNo;            // 申请单号
    private String applyType;          // 申请类型 new/reissue
    private Long userId;               // 申请人ID
    private String applicantName;      // 申请人姓名
    private String contactPhone;       // 联系电话
    private String originalHouseCode;  // 补发时的原门牌号（house_info.house_code），新申请为空
    private String houseType;          // 房屋类型 house/shop/factory
    private String detailAddress;      // 房屋详细地址
    private String reason;             // 申请理由
    private String status;             // 状态 PENDING/APPROVED/REJECTED
    private Long areaId;               // 申请人所在街道（level=4）
    private Integer houseId;           // 通过后分配的三位门牌号（与街道id拼成门牌编号）
    private LocalDateTime createTime;  // 创建时间
}
