package com.company.aegis.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.aegis.modules.system.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {
    void assignPermissions(Long roleId, java.util.List<Long> permissionIds);

    java.util.List<Long> getPermissionIdsByRoleId(Long roleId);
}
