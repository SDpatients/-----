package com.supplier.logistics.controller;

import com.supplier.common.result.Result;
import com.supplier.logistics.dto.DeliveryPackageCreateDTO;
import com.supplier.logistics.service.DeliveryPackageService;
import com.supplier.logistics.vo.DeliveryBarcodeVO;
import com.supplier.logistics.vo.DeliveryPackageVO;
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
@RequestMapping("/v1/delivery-labels")
@RequiredArgsConstructor
public class DeliveryLabelController {

    private final DeliveryPackageService deliveryPackageService;

    /** 查询 ASN 下的箱列表 */
    @GetMapping("/packages/by-notice/{noticeId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DeliveryPackageVO>> listPackages(@PathVariable @NotNull Long noticeId) {
        return Result.success(deliveryPackageService.listByNoticeId(noticeId));
    }

    /** 箱详情（含明细、条码） */
    @GetMapping("/packages/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<DeliveryPackageVO> packageDetail(@PathVariable @NotNull Long id) {
        return Result.success(deliveryPackageService.getDetail(id));
    }

    /** 创建箱信息 */
    @PostMapping("/packages")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> createPackage(@Valid @RequestBody DeliveryPackageCreateDTO dto) {
        return Result.success(deliveryPackageService.create(dto));
    }

    /** 生成条码 */
    @PostMapping("/packages/{id}/barcodes")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DeliveryBarcodeVO>> generateBarcodes(@PathVariable @NotNull Long id) {
        return Result.success(deliveryPackageService.generateBarcodes(id));
    }

    /** 打印标签（记录打印次数） */
    @PostMapping("/packages/{id}/print")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> printLabel(@PathVariable @NotNull Long id) {
        deliveryPackageService.printLabel(id);
        return Result.success();
    }
}