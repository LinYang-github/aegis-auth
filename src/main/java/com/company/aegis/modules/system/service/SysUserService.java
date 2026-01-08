package com.company.aegis.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.aegis.modules.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    void assignRoles(Long userId, java.util.List<Long> roleIds);

    java.util.List<Long> getRoleIdsByUserId(Long userId);

    SysUser getByUsername(String username);
}
