package com.company.aegis.modules.system.controller;

import com.company.aegis.modules.system.entity.SysPermission;
import com.company.aegis.modules.system.service.SysPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SysPermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysPermissionService sysPermissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testListTree() throws Exception {
        SysPermission perm = new SysPermission();
        perm.setId(1L);
        perm.setName("Menu 1");
        when(sysPermissionService.listTree()).thenReturn(Arrays.asList(perm));

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Menu 1"));
    }

    @Test
    void testListTreeWithAppCode() throws Exception {
        SysPermission perm = new SysPermission();
        perm.setId(2L);
        perm.setName("App Menu");
        when(sysPermissionService.listTree("Auth")).thenReturn(Arrays.asList(perm));

        mockMvc.perform(get("/api/permissions?appCode=Auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("App Menu"));
    }

    @Test
    void testCreate() throws Exception {
        SysPermission perm = new SysPermission();
        perm.setName("New Menu");
        when(sysPermissionService.save(any(SysPermission.class))).thenReturn(true);

        mockMvc.perform(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(perm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testUpdate() throws Exception {
        SysPermission perm = new SysPermission();
        perm.setId(1L);
        perm.setName("Updated Menu");
        when(sysPermissionService.updateById(any(SysPermission.class))).thenReturn(true);

        mockMvc.perform(put("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(perm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDelete() throws Exception {
        when(sysPermissionService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }
}
