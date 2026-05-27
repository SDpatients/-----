package com.supplier.order.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.order.dto.OrderChangeApproveDTO;
import com.supplier.order.dto.OrderChangeCreateDTO;
import com.supplier.order.query.OrderChangeQuery;
import com.supplier.order.service.OrderChangeService;
import com.supplier.order.vo.OrderChangeVO;
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
@RequestMapping("/v1/order-changes")
@RequiredArgsConstructor
public class OrderChangeController {

    private final OrderChangeService orderChangeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<OrderChangeVO>> page(@Valid OrderChangeQuery query) {
        return Result.success(orderChangeService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<OrderChangeVO> detail(@PathVariable @NotNull(message = "变更ID不能为空") Long id) {
        return Result.success(orderChangeService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody OrderChangeCreateDTO dto) {
        return Result.success(orderChangeService.create(dto));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approve(@PathVariable @NotNull(message = "变更ID不能为空") Long id,
                                @Valid @RequestBody OrderChangeApproveDTO dto) {
        orderChangeService.approve(id, dto);
        return Result.success();
    }
}
