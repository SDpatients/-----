package com.supplier.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.result.Result;
import com.supplier.order.entity.OrderTrack;
import com.supplier.order.mapper.OrderTrackMapper;
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

    private final OrderTrackMapper orderTrackMapper;

    @Operation(summary = "查询订单全生命周期轨迹")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<OrderTrackVO>> listByOrderId(@PathVariable @NotNull(message = "订单ID不能为空") Long orderId) {
        List<OrderTrack> tracks = orderTrackMapper.selectList(
                new LambdaQueryWrapper<OrderTrack>()
                        .eq(OrderTrack::getOrderId, orderId)
                        .orderByAsc(OrderTrack::getTrackTime));
        List<OrderTrackVO> voList = tracks.stream().map(t -> {
            OrderTrackVO vo = new OrderTrackVO();
            vo.setId(t.getId());
            vo.setOrderId(t.getOrderId());
            vo.setTrackStatus(t.getTrackStatus());
            vo.setTrackTime(t.getTrackTime());
            vo.setTrackRemark(t.getTrackRemark());
            vo.setOperator(t.getOperator());
            vo.setOperatorName(t.getOperatorName());
            return vo;
        }).toList();
        return Result.success(voList);
    }
}