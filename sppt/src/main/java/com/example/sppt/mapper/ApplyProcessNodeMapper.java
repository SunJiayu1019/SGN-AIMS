package com.example.sppt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sppt.entity.ApplyProcessNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批流程节点 Mapper
 * 说明：原先这里有 selectByAreaAndApplyType（从未被调用）和 selectAdminUsers
 *      （与 SysUserMapper.selectAdminUsers 重复，且角色名写的是 '管理员'，
 *       与实际角色编码 coreAdmin/normalAdmin 不一致）。已统一删除，
 *       查询管理员一律走 SysUserService.getAdminUsers()。
 * @author sjy
 * @since 2026-05-28
 */
@Mapper
public interface ApplyProcessNodeMapper extends BaseMapper<ApplyProcessNode> {
}
