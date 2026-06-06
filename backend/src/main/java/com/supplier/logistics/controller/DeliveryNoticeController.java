package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
import com.supplier.logistics.dto.DeliveryNoticeExportRequest;
import com.supplier.logistics.query.DeliveryNoticeQuery;
import com.supplier.logistics.service.DeliveryNoticeService;
import com.supplier.logistics.vo.DeliveryDetailVO;
import com.supplier.logistics.vo.DeliveryNoticeVO;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/delivery-notices")
@RequiredArgsConstructor
public class DeliveryNoticeController {
    private final DeliveryNoticeService deliveryNoticeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<DeliveryNoticeVO>> page(@Valid DeliveryNoticeQuery query) {
        return Result.success(deliveryNoticeService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<DeliveryNoticeVO> detail(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id) {
        return Result.success(deliveryNoticeService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody DeliveryNoticeCreateDTO dto) {
        return Result.success(deliveryNoticeService.create(dto));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> ship(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id, @RequestBody(required = false) DeliveryActionDTO dto) {
        deliveryNoticeService.ship(id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/arrive")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> arrive(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id, @RequestBody(required = false) DeliveryActionDTO dto) {
        deliveryNoticeService.arrive(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}/lines")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DeliveryDetailVO>> lines(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id) {
        return Result.success(deliveryNoticeService.getLines(id));
    }

    @PostMapping("/{id}/warehousing")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> warehousing(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id) {
        deliveryNoticeService.warehousing(id);
        return Result.success();
    }

    @PostMapping("/{id}/scan-receive")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> scanReceive(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id,
                                    @RequestBody(required = false) DeliveryActionDTO dto) {
        deliveryNoticeService.scanReceive(id, dto);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirm-receive")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> confirmReceive(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id,
                                       @RequestBody(required = false) DeliveryActionDTO dto) {
        deliveryNoticeService.confirmReceive(id, dto);
        return Result.success();
    }

    @Operation(summary = "拒收")
    @PostMapping("/{id}/reject-receive")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> rejectReceive(@PathVariable @NotNull(message = "送货通知ID不能为空") Long id,
                                      @RequestBody(required = false) DeliveryActionDTO dto) {
        deliveryNoticeService.rejectReceive(id, dto);
        return Result.success();
    }

    @PostMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> export(@Valid @RequestBody DeliveryNoticeExportRequest request) {
        return Result.success(deliveryNoticeService.export(request));
    }
}
