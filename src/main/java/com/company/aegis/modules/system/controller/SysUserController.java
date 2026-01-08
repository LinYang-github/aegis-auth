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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return Result.success(sysUserService.save(user));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // Do not update password if empty
        }
        return Result.success(sysUserService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            return Result.fail(500, "超级管理员不可删除");
        }
        return Result.success(sysUserService.removeById(id));
    }

    @PostMapping("/{id}/roles")
    public Result<Boolean> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(id, roleIds);
        return Result.success(true);
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoles(@PathVariable Long id) {
        return Result.success(sysUserService.getRoleIdsByUserId(id));
    }
}
