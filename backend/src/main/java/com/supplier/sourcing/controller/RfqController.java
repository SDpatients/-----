package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.PricingDTO;
import com.supplier.sourcing.dto.RfqCreateDTO;
import com.supplier.sourcing.dto.RfqUpdateDTO;
import com.supplier.sourcing.query.RfqQuery;
import com.supplier.sourcing.service.RfqService;
import com.supplier.sourcing.vo.RfqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "询价管理", description = "询价单管理")
@RestController
@RequestMapping("/v1/rfqs")
@RequiredArgsConstructor
public class RfqController {

    private final RfqService rfqService;

    @Operation(summary = "分页查询询价单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<RfqVO>> page(@Valid RfqQuery query) {
        return Result.success(rfqService.page(query));
    }

    @Operation(summary = "查询询价单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<RfqVO> detail(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        return Result.success(rfqService.getDetail(id));
    }

    @Operation(summary = "新增询价单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody RfqCreateDTO dto) {
        return Result.success(rfqService.create(dto));
    }

    @Operation(summary = "更新询价单")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                               @Valid @RequestBody RfqUpdateDTO dto) {
        rfqService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "发布询价单")
    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> publish(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.publish(id);
        return Result.success();
    }

    @Operation(summary = "截止询价")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.close(id);
        return Result.success();
    }

    @Operation(summary = "取消询价单")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable @NotNull(message = "询价单ID不能为空") Long id) {
        rfqService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "定价确认")
    @PostMapping("/{id}/price")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> price(@PathVariable @NotNull(message = "询价单ID不能为空") Long id,
                              @Valid @RequestBody PricingDTO dto) {
        rfqService.price(id, dto);
        return Result.success();
    }
}