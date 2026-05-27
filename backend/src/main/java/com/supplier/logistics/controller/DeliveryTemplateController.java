package com.supplier.logistics.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.logistics.dto.DeliveryTemplateCreateDTO;
import com.supplier.logistics.query.DeliveryTemplateQuery;
import com.supplier.logistics.service.DeliveryTemplateService;
import com.supplier.logistics.vo.DeliveryTemplateVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.supplier.logistics.service.DeliveryPdfService;

@Validated
@RestController
@RequestMapping("/v1/delivery-templates")
@RequiredArgsConstructor
public class DeliveryTemplateController {

    private final DeliveryTemplateService templateService;
    private final DeliveryPdfService pdfService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<DeliveryTemplateVO>> page(@Valid DeliveryTemplateQuery query) {
        return Result.success(templateService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<DeliveryTemplateVO> detail(@PathVariable @NotNull Long id) {
        return Result.success(templateService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody DeliveryTemplateCreateDTO dto) {
        return Result.success(templateService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull Long id, @RequestBody DeliveryTemplateCreateDTO dto) {
        templateService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        templateService.delete(id);
        return Result.success();
    }

    /** 生成送货单 PDF */
    @GetMapping("/pdf/delivery-note/{noticeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> deliveryNotePdf(@PathVariable @NotNull Long noticeId) {
        byte[] pdfBytes = pdfService.generateDeliveryNotePdf(noticeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=delivery_note_" + noticeId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(pdfBytes);
    }

    /** 生成箱标签 */
    @GetMapping("/pdf/package-label/{packageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> packageLabelPdf(@PathVariable @NotNull Long packageId) {
        byte[] pdfBytes = pdfService.generatePackageLabel(packageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=package_label_" + packageId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(pdfBytes);
    }
}