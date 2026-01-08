package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.mapper.SysUserMapper;
import com.company.aegis.modules.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final com.company.aegis.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, java.util.List<Long> roleIds) {
        sysUserRoleMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysUserRole>()
                        .eq(com.company.aegis.modules.system.entity.SysUserRole::getUserId, userId));

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                com.company.aegis.modules.system.entity.SysUserRole ur = new com.company.aegis.modules.system.entity.SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public java.util.List<Long> getRoleIdsByUserId(Long userId) {
        java.util.List<com.company.aegis.modules.system.entity.SysUserRole> list = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysUserRole>()
                        .eq(com.company.aegis.modules.system.entity.SysUserRole::getUserId, userId));
        return list.stream().map(com.company.aegis.modules.system.entity.SysUserRole::getRoleId)
                .collect(java.util.stream.Collectors.toList());
    }
}
