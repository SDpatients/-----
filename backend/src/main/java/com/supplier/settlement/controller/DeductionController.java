package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.DeductionActionDTO;
import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.query.DeductionQuery;
import com.supplier.settlement.service.DeductionService;
import com.supplier.settlement.vo.DeductionVO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "扣款管理")
@RestController
@RequestMapping("/v1/deductions")
@RequiredArgsConstructor
public class DeductionController {

    private final DeductionService deductionService;

    @Operation(summary = "分页查询扣款单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<DeductionVO>> page(@Valid DeductionQuery query) {
        return Result.success(deductionService.page(query));
    }

    @Operation(summary = "查询扣款单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<DeductionVO> detail(@PathVariable @NotNull(message = "扣款单ID不能为空") Long id) {
        return Result.success(deductionService.getDetail(id));
    }

    @Operation(summary = "新增扣款单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody DeductionCreateDTO dto) {
        return Result.success(deductionService.create(dto));
    }

    @Operation(summary = "提交扣款单")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "扣款单ID不能为空") Long id,
                               @RequestBody DeductionActionDTO dto) {
        deductionService.submit(id, dto);
        return Result.success();
    }

    @Operation(summary = "确认扣款单")
    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirm(@PathVariable @NotNull(message = "扣款单ID不能为空") Long id,
                                @RequestBody DeductionActionDTO dto) {
        deductionService.confirm(id, dto);
        return Result.success();
    }

    @Operation(summary = "扣款单异议")
    @PostMapping("/{id}/dispute")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> dispute(@PathVariable @NotNull(message = "扣款单ID不能为空") Long id,
                                @RequestBody DeductionActionDTO dto) {
        deductionService.dispute(id, dto);
        return Result.success();
    }

    @Operation(summary = "扣款单入账")
    @PostMapping("/{id}/book")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> book(@PathVariable @NotNull(message = "扣款单ID不能为空") Long id,
                             @RequestBody DeductionActionDTO dto) {
        deductionService.book(id, dto);
        return Result.success();
    }
}