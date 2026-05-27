package com.supplier.quality.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.quality.dto.QualityAppealAuditDTO;
import com.supplier.quality.dto.QualityAppealCreateDTO;
import com.supplier.quality.query.QualityAppealQuery;
import com.supplier.quality.service.QualityAppealService;
import com.supplier.quality.vo.QualityAppealVO;
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
@Tag(name = "质量申诉")
@RestController
@RequestMapping("/v1/quality-appeals")
@RequiredArgsConstructor
public class QualityAppealController {

    private final QualityAppealService qualityAppealService;

    @Operation(summary = "分页查询质量申诉")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<QualityAppealVO>> page(@Valid QualityAppealQuery query) {
        return Result.success(qualityAppealService.page(query));
    }

    @Operation(summary = "查询质量申诉详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<QualityAppealVO> detail(@PathVariable @NotNull(message = "申诉ID不能为空") Long id) {
        return Result.success(qualityAppealService.getDetail(id));
    }

    @Operation(summary = "创建质量申诉")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody QualityAppealCreateDTO dto) {
        return Result.success(qualityAppealService.create(dto));
    }

    @Operation(summary = "提交质量申诉")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "申诉ID不能为空") Long id) {
        qualityAppealService.submit(id);
        return Result.success();
    }

    @Operation(summary = "通过质量申诉")
    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approve(@PathVariable @NotNull(message = "申诉ID不能为空") Long id,
                                @Valid @RequestBody QualityAppealAuditDTO dto) {
        qualityAppealService.approve(id, dto);
        return Result.success();
    }

    @Operation(summary = "驳回质量申诉")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "申诉ID不能为空") Long id,
                               @Valid @RequestBody QualityAppealAuditDTO dto) {
        qualityAppealService.reject(id, dto);
        return Result.success();
    }
}