package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysRole;
import com.company.aegis.modules.system.mapper.SysRoleMapper;
import com.company.aegis.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final com.company.aegis.modules.system.mapper.SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, java.util.List<Long> permissionIds) {
        // 1. Remove existing
        sysRolePermissionMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysRolePermission>()
                        .eq(com.company.aegis.modules.system.entity.SysRolePermission::getRoleId, roleId));

        // 2. Batch insert
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                com.company.aegis.modules.system.entity.SysRolePermission rp = new com.company.aegis.modules.system.entity.SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public java.util.List<Long> getPermissionIdsByRoleId(Long roleId) {
        java.util.List<com.company.aegis.modules.system.entity.SysRolePermission> list = sysRolePermissionMapper
                .selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysRolePermission>()
                                .eq(com.company.aegis.modules.system.entity.SysRolePermission::getRoleId, roleId));
        return list.stream().map(com.company.aegis.modules.system.entity.SysRolePermission::getPermissionId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<SysRole> list(String appCode) {
        if (appCode == null || appCode.isEmpty()) {
            return list();
        }
        return list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getAppCode, appCode));
    }

    @Override
    public java.util.List<String> getRoleCodes(Long userId, String appCode) {
        // This requires a JOIN or two queries.
        // 1. Get RoleIDs from sys_user_role
        // 2. Get Roles where id IN(ids) AND app_code = appCode

        // Actually we can do it via Mapper XML for efficiency or logic here.
        // Since we don't want to write XML right now, let's use logic.

        // Get all role IDs for user
        // We need SysUserRoleService or Mapper.
        // We have sysRolePermissionMapper, but not UserRoleMapper injected?
        // We need to inject SysUserRoleMapper.
        return baseMapper.selectRoleCodesByUserIdAndAppCode(userId, appCode);
        // Wait, baseMapper is SysRoleMapper. I should add this method to SysRoleMapper
        // interface and XML or annotation.
        // Let's use annotation in Mapper for simplicity.
    }
}
