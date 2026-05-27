package com.supplier.system.controller;

import com.supplier.common.result.Result;
import com.supplier.system.entity.SysConfig;
import com.supplier.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/v1/sys-config")
@RequiredArgsConstructor
public class SysConfigController {
    private final SysConfigService service;

    @Operation(summary = "按类型查询配置列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysConfig>> list(@RequestParam(required = false) Integer configType) {
        return Result.success(service.listByType(configType));
    }

    @Operation(summary = "按类型查询配置键值对")
    @GetMapping("/map")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> map(@RequestParam(required = false) Integer configType) {
        return Result.success(service.getMapByType(configType));
    }

    @Operation(summary = "获取单个配置值")
    @GetMapping("/{configKey}")
    @PreAuthorize("isAuthenticated()")
    public Result<String> getValue(@PathVariable String configKey) {
        return Result.success(service.getValue(configKey));
    }

    @Operation(summary = "保存或更新单个配置")
    @PostMapping
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Void> saveOrUpdate(@RequestBody Map<String, String> body) {
        service.saveOrUpdate(body.get("configKey"), body.get("configValue"), body.get("configName"), body.get("remark"));
        return Result.success();
    }

    @Operation(summary = "批量保存配置")
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('config:manage')")
    public Result<Void> batchSave(@RequestBody List<SysConfig> configs) {
        service.batchSave(configs);
        return Result.success();
    }
}