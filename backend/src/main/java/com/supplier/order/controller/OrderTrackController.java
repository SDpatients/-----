package com.supplier.order.controller;

import com.supplier.common.result.Result;
import com.supplier.order.service.OrderTrackService;
import com.supplier.order.vo.OrderTrackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单交付轨迹查询
 */
@Validated
@Tag(name = "订单交付跟踪", description = "按订单ID查询全生命周期轨迹")
@RestController
@RequestMapping("/v1/order-tracks")
@RequiredArgsConstructor
public class OrderTrackController {

    private final OrderTrackService orderTrackService;

    @Operation(summary = "查询订单全生命周期轨迹")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<OrderTrackVO>> listByOrderId(@PathVariable @NotNull(message = "订单ID不能为空") Long orderId) {
        return Result.success(orderTrackService.listByOrderId(orderId));
    }
}