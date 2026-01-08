package com.company.aegis.modules.system.controller;

import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysPermission;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysPermissionService;
import com.company.aegis.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final SysPermissionService sysPermissionService;
    private final SysUserService sysUserService;

    @GetMapping("/menus")
    public Result<List<SysPermission>> getMenus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // Since we don't have user ID in principal easily (unless we cast), let's fetch
        // user by username.
        // Optimization: CustomUserDetails could hold ID. For now, fetch.
        SysUser user = sysUserService
                .list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username))
                .stream().findFirst().orElse(null);

        if (user == null) {
            return Result.fail(401, "User not found");
        }

        return Result.success(sysPermissionService.getMenusByUserId(user.getId()));
    }
}
