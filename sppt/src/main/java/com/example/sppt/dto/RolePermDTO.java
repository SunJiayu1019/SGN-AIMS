package com.example.sppt.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求参数
 * @author sjy
 */
@Data
public class RolePermDTO {
    private Long roleId;          // 角色ID
    private List<Long> permIds;   // 勾选的权限ID列表
}
