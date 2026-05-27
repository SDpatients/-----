package com.supplier.order.controller;

import com.supplier.common.result.Result;
import com.supplier.order.dto.PurchaseOrderDetailCreateDTO;
import com.supplier.order.dto.PurchaseOrderDetailUpdateDTO;
import com.supplier.order.query.PurchaseOrderDetailQuery;
import com.supplier.order.service.PurchaseOrderDetailService;
import com.supplier.order.vo.PurchaseOrderDetailVO;
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
@RequestMapping("/v1/purchase-order-details")
@RequiredArgsConstructor
public class PurchaseOrderDetailController {

    private final PurchaseOrderDetailService purchaseOrderDetailService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<PurchaseOrderDetailVO>> list(@Valid PurchaseOrderDetailQuery query) {
        return Result.success(purchaseOrderDetailService.list(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<PurchaseOrderDetailVO> detail(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        return Result.success(purchaseOrderDetailService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody PurchaseOrderDetailCreateDTO dto) {
        return Result.success(purchaseOrderDetailService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "明细ID不能为空") Long id,
                               @RequestBody PurchaseOrderDetailUpdateDTO dto) {
        purchaseOrderDetailService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        purchaseOrderDetailService.delete(id);
        return Result.success();
    }
}
