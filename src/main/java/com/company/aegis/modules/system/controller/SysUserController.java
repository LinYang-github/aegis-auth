package com.company.aegis.modules.system.controller;

import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management API
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody SysUser user) {
        // TODO: Password encryption and validation
        return Result.success(sysUserService.save(user));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser user) {
        return Result.success(sysUserService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }
}
