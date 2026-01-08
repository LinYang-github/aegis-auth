package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.aegis.modules.system.entity.SysPermission;
import com.company.aegis.modules.system.mapper.SysPermissionMapper;
import com.company.aegis.modules.system.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
        implements SysPermissionService {

    @Override
    public List<SysPermission> listTree() {
        List<SysPermission> all = list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSortOrder));
        return buildTree(all);
    }

    @Override
    public List<SysPermission> listTree(String appCode) {
        List<SysPermission> all = list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getAppCode, appCode)
                        .orderByAsc(SysPermission::getSortOrder));
        return buildTree(all);
    }

    private final com.company.aegis.modules.system.mapper.SysUserRoleMapper sysUserRoleMapper;
    private final com.company.aegis.modules.system.mapper.SysRolePermissionMapper sysRolePermissionMapper;
    private final com.company.aegis.modules.system.service.SysUserService sysUserService;

    @Override
    public List<SysPermission> getMenusByUserId(Long userId, String appCode) {
        // 1. Check if admin
        com.company.aegis.modules.system.entity.SysUser user = sysUserService.getById(userId);
        if (user != null && "admin".equals(user.getUsername())) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getType, 1)
                    .orderByAsc(SysPermission::getSortOrder);

            if (appCode != null && !appCode.isEmpty()) {
                wrapper.eq(SysPermission::getAppCode, appCode);
            }

            return buildTree(list(wrapper));
        }

        // 2. Get User Roles
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysUserRole>()
                        .eq(com.company.aegis.modules.system.entity.SysUserRole::getUserId, userId))
                .stream().map(com.company.aegis.modules.system.entity.SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty())
            return new ArrayList<>();

        // 3. Get Role Permissions
        // Optimization: We could filter roles by appCode here if we want strict app
        // isolation for roles.
        // But simply filtering the final permissions by appCode is also effective and
        // safer for now if role-app mapping is loose.
        // However, plan said "Role strictly belongs to App".
        // Let's filter permissions by appCode.

        List<Long> permissionIds = sysRolePermissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.company.aegis.modules.system.entity.SysRolePermission>()
                        .in(com.company.aegis.modules.system.entity.SysRolePermission::getRoleId, roleIds))
                .stream().map(com.company.aegis.modules.system.entity.SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        if (permissionIds.isEmpty())
            return new ArrayList<>();

        // 4. Get Permissions
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission> permWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getId, permissionIds)
                .eq(SysPermission::getType, 1)
                .orderByAsc(SysPermission::getSortOrder);

        if (appCode != null && !appCode.isEmpty()) {
            permWrapper.eq(SysPermission::getAppCode, appCode);
        }

        List<SysPermission> permissions = list(permWrapper);

        return buildTree(permissions);
    }

    private List<SysPermission> buildTree(List<SysPermission> all) {
        List<SysPermission> roots = new ArrayList<>();
        Map<Long, SysPermission> map = all.stream().collect(Collectors.toMap(SysPermission::getId, p -> p));

        for (SysPermission p : all) {
            Long parentId = p.getParentId();
            if (parentId == null || parentId == 0 || !map.containsKey(parentId)) {
                roots.add(p);
            } else {
                SysPermission parent = map.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(p);
            }
        }
        return roots;
    }
}
