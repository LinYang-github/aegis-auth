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
}
