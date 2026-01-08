package com.company.aegis.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.aegis.modules.system.entity.SysPermission;
import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {
    List<SysPermission> listTree();

    List<SysPermission> getMenusByUserId(Long userId);
}
