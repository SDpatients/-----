package com.supplier.system.controller;

import com.supplier.common.result.Result;
import com.supplier.system.dto.SysDictCreateDTO;
import com.supplier.system.dto.SysDictItemCreateDTO;
import com.supplier.system.dto.SysDictItemUpdateDTO;
import com.supplier.system.service.SysDictService;
import com.supplier.system.vo.SysDictItemVO;
import com.supplier.system.vo.SysDictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "数据字典", description = "字典及字典项管理接口")
@RestController
@RequestMapping("/v1/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    @Operation(summary = "查询所有字典")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictVO>> listAll() {
        return Result.success(sysDictService.listAll());
    }

    @Operation(summary = "新增字典")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> createDict(@Valid @RequestBody SysDictCreateDTO dto) {
        return Result.success(sysDictService.createDict(dto));
    }

    @Operation(summary = "删除字典（含字典项）")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteDict(@PathVariable @NotNull(message = "字典ID不能为空") Long id) {
        sysDictService.deleteDict(id);
        return Result.success();
    }

    @Operation(summary = "根据字典编码获取字典项列表")
    @GetMapping("/code/{dictCode}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictItemVO>> getItemsByCode(@PathVariable @NotBlank(message = "字典编码不能为空") String dictCode) {
        return Result.success(sysDictService.getItemsByCode(dictCode));
    }

    @Operation(summary = "根据字典ID获取字典项列表")
    @GetMapping("/{dictId}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictItemVO>> getItemsByDictId(@PathVariable @NotNull(message = "字典ID不能为空") Long dictId) {
        return Result.success(sysDictService.getItemsByDictId(dictId));
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> createItem(@Valid @RequestBody SysDictItemCreateDTO dto) {
        return Result.success(sysDictService.createItem(dto));
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateItem(@PathVariable @NotNull(message = "字典项ID不能为空") Long id,
                                   @Valid @RequestBody SysDictItemUpdateDTO dto) {
        sysDictService.updateItem(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteItem(@PathVariable @NotNull(message = "字典项ID不能为空") Long id) {
        sysDictService.deleteItem(id);
        return Result.success();
    }
}