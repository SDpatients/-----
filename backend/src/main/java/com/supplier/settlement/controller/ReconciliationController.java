package com.supplier.settlement.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.settlement.dto.ReconciliationConfirmDTO;
import com.supplier.settlement.dto.ReconciliationCreateDTO;
import com.supplier.settlement.query.ReconciliationQuery;
import com.supplier.settlement.service.ReconciliationService;
import com.supplier.settlement.vo.ReconciliationDetailVO;
import com.supplier.settlement.vo.ReconciliationVO;
import com.supplier.settlement.vo.ThreeWayMatchVO;
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

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/v1/reconciliations")
@RequiredArgsConstructor
public class ReconciliationController {
    private final ReconciliationService reconciliationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ReconciliationVO>> page(@Valid ReconciliationQuery query) {
        return Result.success(reconciliationService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ReconciliationVO> detail(@PathVariable @NotNull(message = "对账单ID不能为空") Long id) {
        return Result.success(reconciliationService.getDetail(id));
    }

    @GetMapping("/{id}/lines")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ReconciliationDetailVO>> lines(@PathVariable @NotNull(message = "对账单ID不能为空") Long id) {
        return Result.success(reconciliationService.getLines(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody ReconciliationCreateDTO dto) {
        return Result.success(reconciliationService.create(dto));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> send(@PathVariable @NotNull(message = "对账单ID不能为空") Long id) {
        reconciliationService.send(id);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirm(@PathVariable @NotNull(message = "对账单ID不能为空") Long id, @RequestBody ReconciliationConfirmDTO dto) {
        reconciliationService.confirm(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/freeze")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> freeze(@PathVariable @NotNull(message = "对账单ID不能为空") Long id,
                               @RequestBody(required = false) Map<String, Object> data) {
        reconciliationService.freeze(id, data);
        return Result.success();
    }

    @PostMapping("/{id}/unfreeze")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> unfreeze(@PathVariable @NotNull(message = "对账单ID不能为空") Long id,
                                 @RequestBody(required = false) Map<String, Object> data) {
        reconciliationService.unfreeze(id, data);
        return Result.success();
    }

    @GetMapping("/{id}/three-way-match")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ThreeWayMatchVO>> threeWayMatch(@PathVariable @NotNull(message = "对账单ID不能为空") Long id) {
        return Result.success(reconciliationService.getThreeWayMatch(id));
    }

    @GetMapping("/{reconId}/invoicable-amount")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> invoicableAmount(@PathVariable @NotNull(message = "对账单ID不能为空") Long reconId) {
        return Result.success(reconciliationService.getInvoicableAmount(reconId));
    }
}
