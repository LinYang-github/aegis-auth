package com.company.aegis.common.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.aegis.modules.system.entity.SysApplication;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysApplicationService;
import com.company.aegis.modules.system.service.SysPermissionService;
import com.company.aegis.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SysUserService sysUserService;
    private final SysPermissionService sysPermissionService;
    private final SysApplicationService sysApplicationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
        initDefaultApp();
        initMenus();
    }

    private void initAdminUser() {
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (count == 0) {
            log.info("Initializing admin user...");
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Default password
            admin.setNickname("System Admin");
            admin.setStatus(1);
            sysUserService.save(admin);
            log.info("Admin user initialized successfully.");
        }
    }

    private void initDefaultApp() {
        long count = sysApplicationService.count(new LambdaQueryWrapper<SysApplication>()
                .eq(SysApplication::getCode, "auth"));
        if (count == 0) {
            SysApplication app = new SysApplication();
            app.setCode("auth");
            app.setName("Aegis Auth System");
            app.setStatus(1);
            sysApplicationService.save(app);
            log.info("Default 'auth' application initialized successfully.");
        }
    }

    private void initMenus() {
        if (sysPermissionService.count() > 0)
            return;

        log.info("Initializing default menus...");

        String authAppCode = "auth";

        // 1. User Management
        com.company.aegis.modules.system.entity.SysPermission userMenu = new com.company.aegis.modules.system.entity.SysPermission();
        userMenu.setName("用户管理");
        userMenu.setCode("sys:user:list"); // Mapping to App.vue activeMenu logic
        userMenu.setType(1);
        userMenu.setPath("/system/user");
        userMenu.setSortOrder(1);
        userMenu.setParentId(0L);
        userMenu.setAppCode(authAppCode);
        sysPermissionService.save(userMenu);

        // 2. Role Management
        com.company.aegis.modules.system.entity.SysPermission roleMenu = new com.company.aegis.modules.system.entity.SysPermission();
        roleMenu.setName("角色管理");
        roleMenu.setCode("sys:role:list");
        roleMenu.setType(1);
        roleMenu.setPath("/system/role");
        roleMenu.setSortOrder(2);
        roleMenu.setParentId(0L);
        roleMenu.setAppCode(authAppCode);
        sysPermissionService.save(roleMenu);

        // 3. Menu Management
        com.company.aegis.modules.system.entity.SysPermission menuMenu = new com.company.aegis.modules.system.entity.SysPermission();
        menuMenu.setName("菜单管理");
        menuMenu.setCode("sys:menu:list");
        menuMenu.setType(1);
        menuMenu.setPath("/system/menu");
        menuMenu.setSortOrder(3);
        menuMenu.setParentId(0L);
        menuMenu.setAppCode(authAppCode);
        sysPermissionService.save(menuMenu);

        // 4. App Management (New)
        com.company.aegis.modules.system.entity.SysPermission appMenu = new com.company.aegis.modules.system.entity.SysPermission();
        appMenu.setName("应用管理");
        appMenu.setCode("sys:app:list");
        appMenu.setType(1);
        appMenu.setPath("/system/application");
        appMenu.setSortOrder(4);
        appMenu.setParentId(0L);
        appMenu.setAppCode(authAppCode);
        sysPermissionService.save(appMenu);

        log.info("Default menus initialized.");
    }
}
