package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.query.MaterialQuery;
import com.supplier.sourcing.service.MaterialService;
import com.supplier.sourcing.vo.MaterialVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "物料主数据", description = "物料信息查询接口")
@RestController
@RequestMapping("/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "分页查询物料")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<MaterialVO>> page(@Valid MaterialQuery query) {
        return Result.success(materialService.page(query));
    }

    @Operation(summary = "查询物料详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<MaterialVO> detail(@PathVariable @NotNull(message = "物料ID不能为空") Long id) {
        return Result.success(materialService.getDetail(id));
    }
}