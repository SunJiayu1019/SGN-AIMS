package com.example.sppt.entity;

/**
 * 门牌信息表
 * @author sjy
 * @since 2026-05-27
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("house_info")
public class HouseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;            // 主键ID
    private String houseCode;   // 门牌编号
    private String address;     // 地址
    private String houseType;   // 类型 house/resume/shop 住宅/商铺/厂房
    private String lng;         // 经度
    private String lat;         // 纬度
    private String geometry;    // 地理坐标 POINT(116.4 39.9)
    private Long areaId;        // 区域ID
    private Integer status;     // 状态
}