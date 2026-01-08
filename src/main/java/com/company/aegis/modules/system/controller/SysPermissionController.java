package com.company.aegis.modules.system.controller;

import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysPermission;
import com.company.aegis.modules.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Permission/Menu Management API
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    @GetMapping
    public Result<List<SysPermission>> list() {
        return Result.success(sysPermissionService.listTree());
    }

    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody SysPermission permission) {
        return Result.success(sysPermissionService.save(permission));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysPermission permission) {
        return Result.success(sysPermissionService.updateById(permission));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysPermissionService.removeById(id));
    }
}
