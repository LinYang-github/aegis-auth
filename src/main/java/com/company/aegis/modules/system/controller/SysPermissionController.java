package com.company.aegis.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysPermission;
import com.company.aegis.modules.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    @GetMapping
    public Result<List<SysPermission>> list(@RequestParam(required = false) String appCode) {
        if (appCode != null && !appCode.isEmpty()) {
            // For simplicity, we can't easily pass wrapper to listTree if it doesn't
            // support it.
            // We should add listTree(appCode) to service.
            return Result.success(sysPermissionService.listTree(appCode));
        }
        return Result.success(sysPermissionService.listTree());
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
