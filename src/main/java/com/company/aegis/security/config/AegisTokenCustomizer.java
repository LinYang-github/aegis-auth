package com.company.aegis.security.config;

import com.company.aegis.modules.system.entity.SysRole;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysRoleService;
import com.company.aegis.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AegisTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    @Override
    public void customize(JwtEncodingContext context) {
        // Only customize Access Token and ID Token
        /*
         * Note: Spring Auth Server by default puts scopes.
         * We want to add 'roles' and 'app_code'.
         */

        Authentication principal = context.getPrincipal();
        String username = principal.getName();

        // Context has RegisteredClient
        String clientId = context.getRegisteredClient().getClientId(); // This is app_code

        // Inject app_code
        context.getClaims().claim("app_code", clientId);

        // Find User
        // Note: Principal might be UsernamePasswordAuthenticationToken or similar
        // We fetch user from DB to be sure or rely on UserDetails if available.
        // Let's fetch roles by UserID + AppCode

        SysUser user = sysUserService.getByUsername(username);
        if (user != null) {
            // Fetch roles for this app
            // SysRoleService needs a method: getRolesByUserIdAndAppCode(userId, appCode)
            // Currently we don't have it exposed efficiently.
            // We have `sysUserRoleMapper` relationship.
            // Let's implement a quick check in SysRoleService or here using stream if list
            // is small,
            // but better to add a service method.

            // For now, let's assume we add getRoles(userId, appCode) to SysRoleService
            List<String> roleCodes = sysRoleService.getRoleCodes(user.getId(), clientId);

            context.getClaims().claim("roles", roleCodes);

            // Also user info
            context.getClaims().claim("nickname", user.getNickname());
            context.getClaims().claim("avatar", user.getAvatar());
        }
    }
}
