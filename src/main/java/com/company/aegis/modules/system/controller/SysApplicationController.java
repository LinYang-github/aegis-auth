package com.company.aegis.modules.system.controller;

import com.company.aegis.common.result.Result;
import com.company.aegis.modules.system.entity.SysApplication;
import com.company.aegis.modules.system.service.SysApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class SysApplicationController {

    private final SysApplicationService sysApplicationService;
    private final com.company.aegis.modules.system.service.OidcClientService oidcClientService;

    @GetMapping
    public Result<List<SysApplication>> list() {
        return Result.success(sysApplicationService.list());
    }

    // ... existing CRUD ...

    @GetMapping("/{id}")
    public Result<SysApplication> getById(@PathVariable Long id) {
        return Result.success(sysApplicationService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody SysApplication app) {
        return Result.success(sysApplicationService.save(app));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysApplication app) {
        return Result.success(sysApplicationService.updateById(app));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysApplicationService.removeById(id));
    }

    // OIDC Endpoints

    @GetMapping("/{id}/oidc")
    public Result<com.company.aegis.modules.system.dto.OidcConfigDto> getOidcConfig(@PathVariable Long id) {
        return Result.success(oidcClientService.getOidcConfig(id));
    }

    @PostMapping("/{id}/oidc")
    public Result<Boolean> updateOidcConfig(@PathVariable Long id,
            @RequestBody com.company.aegis.modules.system.dto.OidcConfigDto config) {
        oidcClientService.updateOidcConfig(id, config);
        return Result.success(true);
    }

    @PostMapping("/{id}/oidc/rotate-secret")
    public Result<String> rotateSecret(@PathVariable Long id) {
        return Result.success(oidcClientService.rotateSecret(id));
    }
}
