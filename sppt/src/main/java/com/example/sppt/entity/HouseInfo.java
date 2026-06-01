package com.example.sppt.entity;

/**
 * 门牌信息表
 *
 * 与「数据库重制版」对齐，补上此前实体遗漏的两列：
 *  - applyNo     关联的申请号（house_info.apply_no，UNIQUE，外键指向 apply_form.apply_no）
 *  - createTime  创建时间（house_info.create_time）
 *
 * 说明：此前实体缺失这两列，导致 MyBatis-Plus 生成的 INSERT 未覆盖
 *      NOT NULL 的 apply_no，门牌入库/「门牌排查」查询都会异常 —— 故一并补全。
 *
 * 门牌编号 houseCode 规则：{街道areaId}-{三位门牌号}，如 "10086-001"。
 * @author sjy
 * @since 2026-05-31
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("house_info")
public class HouseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;                  // 主键ID
    private String applyNo;           // 关联的申请号（UNIQUE）
    private String houseCode;         // 门牌编号 如 "10086-001"
    private String address;           // 地址
    private String houseType;         // 类型 house/shop/factory 住宅/商铺/厂房
    private String lng;               // 经度
    private String lat;               // 纬度
    private String geometry;          // 地理坐标 POINT(116.4 39.9)
    private Long areaId;              // 门牌所在街道（level=4）
    private LocalDateTime createTime; // 创建时间
    private Integer status;           // 状态 1有效 0无效
}
