package com.company.aegis.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.aegis.common.annotation.AutoLog;
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
    @AutoLog("List Users")
    public Result<IPage<SysUser>> list(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysUserService.page(new Page<>(page, size)));
    }

    @GetMapping("/{id}")
    @AutoLog("Get User")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    @AutoLog("Create User")
    public Result<Boolean> create(@RequestBody SysUser sysUser) {
        if (sysUser.getPassword() != null) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        return Result.success(sysUserService.save(sysUser));
    }

    @PutMapping
    @AutoLog("Update User")
    public Result<Boolean> update(@RequestBody SysUser sysUser) {
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        } else {
            sysUser.setPassword(null); // Do not update password if empty
        }
        return Result.success(sysUserService.updateById(sysUser));
    }

    @DeleteMapping("/{id}")
    @AutoLog("Delete User")
    public Result<Boolean> delete(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            return Result.fail(500, "超级管理员不可删除");
        }
        return Result.success(sysUserService.removeById(id));
    }

    @PostMapping("/{userId}/roles")
    @AutoLog("Assign Roles")
    public Result<Boolean> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return Result.success(true);
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoles(@PathVariable Long id) {
        return Result.success(sysUserService.getRoleIdsByUserId(id));
    }
}
