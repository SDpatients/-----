package com.supplier.logistics.controller;

import com.supplier.common.result.Result;
import com.supplier.logistics.dto.DeliveryDetailCreateDTO;
import com.supplier.logistics.dto.DeliveryDetailUpdateDTO;
import com.supplier.logistics.query.DeliveryDetailQuery;
import com.supplier.logistics.service.DeliveryDetailService;
import com.supplier.logistics.vo.DeliveryDetailVO;
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
@RequestMapping("/v1/delivery-details")
@RequiredArgsConstructor
public class DeliveryDetailController {

    private final DeliveryDetailService deliveryDetailService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<DeliveryDetailVO>> list(@Valid DeliveryDetailQuery query) {
        return Result.success(deliveryDetailService.list(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<DeliveryDetailVO> detail(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        return Result.success(deliveryDetailService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody DeliveryDetailCreateDTO dto) {
        return Result.success(deliveryDetailService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "明细ID不能为空") Long id, @RequestBody DeliveryDetailUpdateDTO dto) {
        deliveryDetailService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "明细ID不能为空") Long id) {
        deliveryDetailService.delete(id);
        return Result.success();
    }
}
