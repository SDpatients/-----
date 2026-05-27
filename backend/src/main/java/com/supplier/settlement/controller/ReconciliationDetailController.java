package com.supplier.settlement.controller;

import com.supplier.common.result.Result;
import com.supplier.settlement.dto.ReconciliationDetailCreateDTO;
import com.supplier.settlement.dto.ReconciliationDetailUpdateDTO;
import com.supplier.settlement.query.ReconciliationDetailQuery;
import com.supplier.settlement.service.ReconciliationDetailService;
import com.supplier.settlement.vo.ReconciliationDetailVO;
import jakarta.validation.Valid;
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
@RestController
@RequestMapping("/v1/reconciliation-details")
@RequiredArgsConstructor
public class ReconciliationDetailController {

    private final ReconciliationDetailService reconciliationDetailService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<ReconciliationDetailVO>> list(@Valid ReconciliationDetailQuery query) {
        return Result.success(reconciliationDetailService.list(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ReconciliationDetailVO> detail(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        return Result.success(reconciliationDetailService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ReconciliationDetailCreateDTO dto) {
        return Result.success(reconciliationDetailService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "明细ID不能为空") Long id, @RequestBody ReconciliationDetailUpdateDTO dto) {
        reconciliationDetailService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        reconciliationDetailService.delete(id);
        return Result.success();
    }
}
