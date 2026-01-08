package com.company.aegis.modules.system.controller;

import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SysUserController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for unit testing
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysUserService sysUserService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testList() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        when(sysUserService.list()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void testGetById() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        when(sysUserService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreate() throws Exception {
        SysUser user = new SysUser();
        user.setUsername("newuser");
        user.setPassword("rawpass");

        when(passwordEncoder.encode("rawpass")).thenReturn("encoded");
        when(sysUserService.save(any(SysUser.class))).thenReturn(true);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(passwordEncoder).encode("rawpass");
        verify(sysUserService).save(any(SysUser.class));
    }

    @Test
    void testUpdate() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("updated");

        when(sysUserService.updateById(any(SysUser.class))).thenReturn(true);

        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDelete() throws Exception {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("normalUser");
        when(sysUserService.getById(2L)).thenReturn(user);
        when(sysUserService.removeById(2L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDeleteAdmin_Fail() throws Exception {
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername("admin");
        when(sysUserService.getById(1L)).thenReturn(admin);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk()) // It returns 200 OK HTTP status but specific code in Result
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("超级管理员不可删除"));
    }

    @Test
    void testAssignRoles() throws Exception {
        java.util.List<Long> roleIds = Arrays.asList(100L);

        mockMvc.perform(post("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(sysUserService).assignRoles(eq(1L), any());
    }

    @Test
    void testGetRoles() throws Exception {
        when(sysUserService.getRoleIdsByUserId(1L)).thenReturn(Arrays.asList(100L));

        mockMvc.perform(get("/api/users/1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(100));
    }
}
