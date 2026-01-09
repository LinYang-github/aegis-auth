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
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SysUserService sysUserService;
    private final SysPermissionService sysPermissionService;
    private final SysApplicationService sysApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public void run(String... args) throws Exception {
        initSchema();
        initAdminUser();
        initDefaultApp();
        initDemoClient();
        initVueDemoClient();
        initGoDemoClient();
        initMenus();
    }

    private void initSchema() {
        // 1. SysLog (Create if not exists)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "operation TEXT," +
                "method TEXT," +
                "params TEXT," +
                "time INTEGER," +
                "ip TEXT," +
                "create_time TEXT" +
                ")");
        log.info("Schema initialized (sys_log checked).");
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

    private void initDemoClient() {
        String appCode = "demo-client";
        long count = sysApplicationService.count(new LambdaQueryWrapper<SysApplication>()
                .eq(SysApplication::getCode, appCode));
        if (count == 0) {
            // ... (Same as before)
            SysApplication app = new SysApplication();
            app.setCode(appCode);
            app.setName("Demo Client App");
            app.setStatus(1);
            sysApplicationService.save(app);

            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(appCode)
                    .clientSecret(passwordEncoder.encode("demo-secret"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://127.0.0.1:8080/login/oauth2/code/aegis")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                    .build();
            registeredClientRepository.save(registeredClient);
            log.info("Demo Client 'demo-client' initialized successfully.");
        }
    }

    private void initVueDemoClient() {
        String appCode = "demo-vue-client";
        long count = sysApplicationService.count(new LambdaQueryWrapper<SysApplication>()
                .eq(SysApplication::getCode, appCode));
        if (count == 0) {
            // 1. SysApplication
            SysApplication app = new SysApplication();
            app.setCode(appCode);
            app.setName("Demo Vue Client (SPA)");
            app.setStatus(1);
            sysApplicationService.save(app);

            // 2. RegisteredClient (Public / PKCE)
            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(appCode)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:5173/callback")
                    .postLogoutRedirectUri("http://localhost:5173/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .requireProofKey(true) // Enforce PKCE
                            .build())
                    .build();

            registeredClientRepository.save(registeredClient);
            log.info("Demo Client 'demo-vue-client' initialized successfully.");
        }
    }

    private void initGoDemoClient() {
        String appCode = "demo-go-client";

        // 1. Ensure SysApplication exists
        long count = sysApplicationService.count(new LambdaQueryWrapper<SysApplication>()
                .eq(SysApplication::getCode, appCode));
        if (count == 0) {
            SysApplication app = new SysApplication();
            app.setName("Go OIDC Demo");
            app.setCode(appCode);
            app.setStatus(1);
            sysApplicationService.save(app);
        }

        // 2. Always ensure RegisteredClient exists and has correct secret
        RegisteredClient existingClient = registeredClientRepository.findByClientId(appCode);
        if (existingClient != null) {
            // Update existing (reset secret)
            RegisteredClient updatedClient = RegisteredClient.from(existingClient)
                    .clientSecret(passwordEncoder.encode("demo-go-secret"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8081/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                    .build();
            registeredClientRepository.save(updatedClient);
            log.info("Demo Client '{}' updated successfully.", appCode);
        } else {
            // Create new
            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(appCode)
                    .clientSecret(passwordEncoder.encode("demo-go-secret"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // For Debugging
                    .redirectUri("http://localhost:8081/callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                    .build();

            registeredClientRepository.save(registeredClient);
            log.info("Demo Client '{}' initialized successfully.", appCode);
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
