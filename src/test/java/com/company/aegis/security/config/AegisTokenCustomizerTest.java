package com.company.aegis.security.config;

import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysRoleService;
import com.company.aegis.modules.system.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AegisTokenCustomizerTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysRoleService sysRoleService;

    @InjectMocks
    private AegisTokenCustomizer aegisTokenCustomizer;

    @Mock
    private JwtEncodingContext context;

    @Mock
    private RegisteredClient registeredClient;

    @Mock
    private JwtClaimsSet.Builder claimsBuilder;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Test
    void testCustomize() {
        // Mock Context inputs
        when(context.getPrincipal()).thenReturn(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("testuser",
                        "password"));
        when(context.getRegisteredClient()).thenReturn(registeredClient);
        when(registeredClient.getClientId()).thenReturn("test-client-id");
        when(context.getClaims()).thenReturn(claimsBuilder);

        // Mock Service calls
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("Test User");
        user.setAvatar("avatar.png");
        when(sysUserService.getByUsername("testuser")).thenReturn(user);

        when(sysRoleService.getRoleCodes(1L, "test-client-id")).thenReturn(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        // Execute
        aegisTokenCustomizer.customize(context);

        // Verify Claims
        verify(claimsBuilder).claim("app_code", "test-client-id");
        verify(claimsBuilder).claim("roles", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        verify(claimsBuilder).claim("nickname", "Test User");
        verify(claimsBuilder).claim("avatar", "avatar.png");
    }

    @Test
    void testCustomize_UserNotFound() {
        // Mock Context inputs
        when(context.getPrincipal()).thenReturn(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("unknown",
                        "password"));
        when(context.getRegisteredClient()).thenReturn(registeredClient);
        when(registeredClient.getClientId()).thenReturn("test-client-id");
        when(context.getClaims()).thenReturn(claimsBuilder);

        // Mock Service calls
        when(sysUserService.getByUsername("unknown")).thenReturn(null);

        // Execute
        aegisTokenCustomizer.customize(context);

        // Verify Claims (app_code should still be present)
        verify(claimsBuilder).claim("app_code", "test-client-id");
        // User specific claims should NOT be present
        verify(claimsBuilder, never()).claim(eq("roles"), any());
        verify(claimsBuilder, never()).claim(eq("nickname"), any());
    }
}
