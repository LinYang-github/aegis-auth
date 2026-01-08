package com.company.aegis.modules.system.controller;

import com.company.aegis.modules.system.entity.SysRole;
import com.company.aegis.modules.system.service.SysRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SysRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysRoleService sysRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testList() throws Exception {
        SysRole role = new SysRole();
        role.setId(1L);
        role.setCode("ADMIN");
        when(sysRoleService.list(any(String.class))).thenReturn(Arrays.asList(role));

        mockMvc.perform(get("/api/roles").param("appCode", "Auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].code").value("ADMIN"));
    }

    @Test
    void testGetById() throws Exception {
        SysRole role = new SysRole();
        role.setId(1L);
        when(sysRoleService.getById(1L)).thenReturn(role);

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreate() throws Exception {
        SysRole role = new SysRole();
        role.setName("New Role");
        when(sysRoleService.save(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testUpdate() throws Exception {
        SysRole role = new SysRole();
        role.setId(1L);
        role.setName("Updated Role");
        when(sysRoleService.updateById(any(SysRole.class))).thenReturn(true);

        mockMvc.perform(put("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDelete() throws Exception {
        when(sysRoleService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testAssignPermissions() throws Exception {
        List<Long> permissionIds = Arrays.asList(10L, 20L);

        mockMvc.perform(post("/api/roles/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(sysRoleService).assignPermissions(eq(1L), any());
    }

    @Test
    void testGetPermissions() throws Exception {
        when(sysRoleService.getPermissionIdsByRoleId(1L)).thenReturn(Arrays.asList(10L, 20L));

        mockMvc.perform(get("/api/roles/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").value(10));
    }
}
