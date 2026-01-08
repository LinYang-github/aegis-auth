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

    @GetMapping
    public Result<List<SysApplication>> list() {
        return Result.success(sysApplicationService.list());
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
}
