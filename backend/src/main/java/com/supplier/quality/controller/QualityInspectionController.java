package com.supplier.quality.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.quality.dto.QualityHandleDTO;
import com.supplier.quality.dto.QualityInspectionCreateDTO;
import com.supplier.quality.dto.QualitySubmitDTO;
import com.supplier.quality.query.QualityInspectionQuery;
import com.supplier.quality.service.QualityInspectionService;
import com.supplier.quality.vo.QualityInspectionVO;
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
@RestController
@RequestMapping("/v1/quality-inspections")
@RequiredArgsConstructor
public class QualityInspectionController {
    private final QualityInspectionService qualityInspectionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<QualityInspectionVO>> page(@Valid QualityInspectionQuery query) {
        return Result.success(qualityInspectionService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<QualityInspectionVO> detail(@PathVariable @NotNull(message = "质检单ID不能为空") Long id) {
        return Result.success(qualityInspectionService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody QualityInspectionCreateDTO dto) {
        return Result.success(qualityInspectionService.create(dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "质检单ID不能为空") Long id, @RequestBody QualitySubmitDTO dto) {
        qualityInspectionService.submit(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> handle(@PathVariable @NotNull(message = "质检单ID不能为空") Long id, @Valid @RequestBody QualityHandleDTO dto) {
        qualityInspectionService.handle(id, dto);
        return Result.success();
    }
}
