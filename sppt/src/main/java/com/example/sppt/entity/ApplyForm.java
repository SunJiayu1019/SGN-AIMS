package com.example.sppt.entity;

/**
 * 门牌申请表
 *
 * 关于本次新增字段（用于打通"群众申请 -> 管理员审批 -> 结果回显"的数据衔接）：
 *  - applicantName  申请人姓名：申请时填写的姓名快照，审批列表直接展示，无需再去 join sys_user
 *  - houseType      房屋类型：house 住宅 / shop 商铺 / factory 厂房
 *  - detailAddress  房屋详细地址：新申请时门牌尚未生成（house_info 还没有这条记录），
 *                   所以申请人填写的地址必须落在申请表上，否则无处存放、无法审批
 *  - areaId         所属区域ID：apply_form 表本来就有 area_id 列，但旧实体类漏映射了，
 *                   导致区域筛选 / 数据权限都拿不到值，这里补上
 *
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
    private String applyNo;          // 申请单号
    private String applyType;        // 申请类型 new/reissue
    private Long userId;             // 申请人ID
    private String applicantName;    // 申请人姓名（新增）
    private String contactPhone;     // 联系电话
    private Long houseId;            // 门牌ID（补发时关联已有门牌；新申请时为空）
    private String houseType;        // 房屋类型 house/shop/factory（新增）
    private String detailAddress;    // 房屋详细地址（新增）
    private String reason;           // 申请理由
    private String status;           // 状态 PENDING/APPROVED/REJECTED
    private Long areaId;             // 所属区域ID（补映射）
    private LocalDateTime createTime;
}
