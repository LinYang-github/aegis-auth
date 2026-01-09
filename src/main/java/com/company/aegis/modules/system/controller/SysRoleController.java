package com.company.aegis.modules.system.controller;

import com.company.aegis.common.annotation.AutoLog;
import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysRole;
import com.company.aegis.modules.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Management API
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @AutoLog("List Roles")
    public Result<List<SysRole>> list(@RequestParam(required = false) String appCode) {
        return Result.success(sysRoleService.list(appCode));
    }

    @GetMapping("/{id}")
    @AutoLog("Get Role")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @PostMapping
    @AutoLog("Create Role")
    public Result<Boolean> create(@RequestBody SysRole sysRole) {
        return Result.success(sysRoleService.save(sysRole));
    }

    @PutMapping
    @AutoLog("Update Role")
    public Result<Boolean> update(@RequestBody SysRole sysRole) {
        return Result.success(sysRoleService.updateById(sysRole));
    }

    @DeleteMapping("/{id}")
    @AutoLog("Delete Role")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysRoleService.removeById(id));
    }

    @PostMapping("/{roleId}/permissions")
    @AutoLog("Assign Permissions")
    public Result<Boolean> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        sysRoleService.assignPermissions(roleId, permissionIds);
        return Result.success(true);
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getPermissionIdsByRoleId(id));
    }
}
