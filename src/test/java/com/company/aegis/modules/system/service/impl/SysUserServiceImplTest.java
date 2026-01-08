package com.company.aegis.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.aegis.modules.system.dto.ChangePasswordDto;
import com.company.aegis.modules.system.dto.UserProfileDto;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.entity.SysUserRole;
import com.company.aegis.modules.system.mapper.SysUserMapper;
import com.company.aegis.modules.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Manually inject baseMapper for ServiceImpl
        org.springframework.test.util.ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
    }

    @Test
    void testAssignRoles() {
        Long userId = 1L;
        java.util.List<Long> roleIds = Arrays.asList(100L, 101L);

        sysUserService.assignRoles(userId, roleIds);

        // Verify deletion of old roles
        verify(sysUserRoleMapper, times(1)).delete(any(LambdaQueryWrapper.class));

        // Verify insertion of new roles
        verify(sysUserRoleMapper, times(2)).insert(any(SysUserRole.class));
    }

    @Test
    void testAssignRoles_Empty() {
        Long userId = 1L;
        sysUserService.assignRoles(userId, Collections.emptyList());

        verify(sysUserRoleMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(sysUserRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    void testUpdateProfile() {
        Long userId = 1L;
        UserProfileDto dto = new UserProfileDto();
        dto.setNickname("New Nickname");
        dto.setEmail("new@example.com");

        SysUser existingUser = new SysUser();
        existingUser.setId(userId);
        existingUser.setNickname("Old Nickname");

        when(sysUserMapper.selectById(userId)).thenReturn(existingUser);
        when(sysUserMapper.updateById(existingUser)).thenReturn(1); // 1 row updated

        sysUserService.updateProfile(userId, dto);

        assertEquals("New Nickname", existingUser.getNickname());
        assertEquals("new@example.com", existingUser.getEmail());
        verify(sysUserMapper, times(1)).updateById(eq(existingUser));
    }

    @Test
    void testChangePassword_Success() {
        Long userId = 1L;
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass");
        dto.setConfirmPassword("newPass");

        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword("encodedOldPass");

        when(sysUserMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        sysUserService.changePassword(userId, dto);

        assertEquals("encodedNewPass", user.getPassword());
        verify(sysUserMapper, times(1)).updateById(eq(user));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        Long userId = 1L;
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("wrongPass");

        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword("encodedOldPass");

        when(sysUserMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> sysUserService.changePassword(userId, dto));
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void testChangePassword_Mismatch() {
        Long userId = 1L;
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass");
        dto.setConfirmPassword("mismatch");

        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword("encodedOldPass");

        when(sysUserMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> sysUserService.changePassword(userId, dto));
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }
}
