package com.supplier.order.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.order.dto.BuyerConfirmDTO;
import com.supplier.order.dto.DeliveryFeedbackCreateDTO;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.OrderCloseDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.query.PurchaseOrderQuery;
import com.supplier.order.service.DeliveryFeedbackService;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.order.vo.DeliveryFeedbackVO;
import com.supplier.order.vo.PurchaseOrderVO;
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
@Tag(name = "采购订单协同", description = "采购订单查询、下发、确认、拒单和取消接口")
@RestController
@RequestMapping("/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final DeliveryFeedbackService deliveryFeedbackService;

    @Operation(summary = "分页查询采购订单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<PurchaseOrderVO>> page(@Valid PurchaseOrderQuery query) {
        return Result.success(purchaseOrderService.page(query));
    }

    @Operation(summary = "查询采购订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<PurchaseOrderVO> detail(@PathVariable @NotNull(message = "订单ID不能为空") Long id) {
        return Result.success(purchaseOrderService.getDetail(id));
    }

    @Operation(summary = "新增采购订单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody PurchaseOrderCreateDTO dto) {
        return Result.success(purchaseOrderService.create(dto));
    }

    @Operation(summary = "下发采购订单")
    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> publish(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                                @RequestBody OrderActionDTO dto) {
        purchaseOrderService.publish(id, dto);
        return Result.success();
    }

    @Operation(summary = "供应商确认订单")
    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirm(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                                @RequestBody OrderActionDTO dto) {
        purchaseOrderService.confirm(id, dto);
        return Result.success();
    }

    @Operation(summary = "供应商拒绝订单")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                               @RequestBody OrderActionDTO dto) {
        purchaseOrderService.reject(id, dto);
        return Result.success();
    }

    @Operation(summary = "取消采购订单")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                               @RequestBody OrderActionDTO dto) {
        purchaseOrderService.cancel(id, dto);
        return Result.success();
    }

    @Operation(summary = "采购方确认供应商接单结果")
    @PostMapping("/{id}/confirm-by-buyer")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmByBuyer(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                                       @RequestBody BuyerConfirmDTO dto) {
        purchaseOrderService.confirmByBuyer(id, dto);
        return Result.success();
    }

    @Operation(summary = "关闭采购订单")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                              @RequestBody OrderCloseDTO dto) {
        purchaseOrderService.close(id, dto);
        return Result.success();
    }

    @Operation(summary = "供应商提交交期反馈")
    @PostMapping("/{id}/delivery-feedback")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> submitDeliveryFeedback(@PathVariable @NotNull(message = "订单ID不能为空") Long id,
                                               @Valid @RequestBody DeliveryFeedbackCreateDTO dto) {
        return Result.success(deliveryFeedbackService.submit(id, dto));
    }

    @Operation(summary = "查询交期反馈")
    @GetMapping("/{id}/delivery-feedback")
    @PreAuthorize("isAuthenticated()")
    public Result<DeliveryFeedbackVO> getDeliveryFeedback(@PathVariable @NotNull(message = "订单ID不能为空") Long id) {
        return Result.success(deliveryFeedbackService.getByOrderId(id));
    }

    @Operation(summary = "采购方确认交期反馈")
    @PostMapping("/delivery-feedback/{feedbackId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmDeliveryFeedback(@PathVariable @NotNull(message = "反馈ID不能为空") Long feedbackId) {
        deliveryFeedbackService.confirmByBuyer(feedbackId);
        return Result.success();
    }
}
