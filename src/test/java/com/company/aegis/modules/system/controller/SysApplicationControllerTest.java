package com.company.aegis.modules.system.controller;

import com.company.aegis.modules.system.dto.OidcConfigDto;
import com.company.aegis.modules.system.entity.SysApplication;
import com.company.aegis.modules.system.service.OidcClientService;
import com.company.aegis.modules.system.service.SysApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = SysApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysApplicationService sysApplicationService;

    @MockBean
    private OidcClientService oidcClientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testList() throws Exception {
        SysApplication app = new SysApplication();
        app.setId(1L);
        app.setName("Test App");
        when(sysApplicationService.list()).thenReturn(Arrays.asList(app));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("Test App"));
    }

    @Test
    void testGetById() throws Exception {
        SysApplication app = new SysApplication();
        app.setId(1L);
        when(sysApplicationService.getById(1L)).thenReturn(app);

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreate() throws Exception {
        SysApplication app = new SysApplication();
        app.setName("New App");
        when(sysApplicationService.save(any(SysApplication.class))).thenReturn(true);

        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(app)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testUpdate() throws Exception {
        SysApplication app = new SysApplication();
        app.setId(1L);
        app.setName("Updated App");
        when(sysApplicationService.updateById(any(SysApplication.class))).thenReturn(true);

        mockMvc.perform(put("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(app)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDelete() throws Exception {
        when(sysApplicationService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testGetOidcConfig() throws Exception {
        OidcConfigDto config = new OidcConfigDto();
        config.setClientId("test-client");
        when(oidcClientService.getOidcConfig(1L)).thenReturn(config);

        mockMvc.perform(get("/api/applications/1/oidc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.clientId").value("test-client"));
    }

    @Test
    void testUpdateOidcConfig() throws Exception {
        OidcConfigDto config = new OidcConfigDto();
        config.setRedirectUris(Collections.singleton("http://localhost/callback"));

        mockMvc.perform(post("/api/applications/1/oidc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(oidcClientService).updateOidcConfig(eq(1L), any(OidcConfigDto.class));
    }

    @Test
    void testRotateSecret() throws Exception {
        when(oidcClientService.rotateSecret(1L)).thenReturn("new-secret");

        mockMvc.perform(post("/api/applications/1/oidc/rotate-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("new-secret"));
    }
}
