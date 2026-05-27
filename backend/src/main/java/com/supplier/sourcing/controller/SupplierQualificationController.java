package com.supplier.sourcing.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.sourcing.dto.SupplierQualificationCreateDTO;
import com.supplier.sourcing.dto.SupplierQualificationUpdateDTO;
import com.supplier.sourcing.query.SupplierQualificationQuery;
import com.supplier.sourcing.service.SupplierQualificationService;
import com.supplier.sourcing.vo.SupplierQualificationVO;
import com.supplier.system.service.SysFileAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Validated
@Tag(name = "供应商资质", description = "供应商资质管理")
@RestController
@RequestMapping("/v1/supplier-qualifications")
@RequiredArgsConstructor
public class SupplierQualificationController {

    private final SupplierQualificationService supplierQualificationService;
    private final SysFileAttachmentService fileAttachmentService;

    @Operation(summary = "分页查询资质")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SupplierQualificationVO>> page(@Valid SupplierQualificationQuery query) {
        return Result.success(supplierQualificationService.page(query));
    }

    @Operation(summary = "查询资质详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SupplierQualificationVO> detail(@PathVariable @NotNull(message = "资质ID不能为空") Long id) {
        return Result.success(supplierQualificationService.getDetail(id));
    }

    @Operation(summary = "新增资质")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SupplierQualificationCreateDTO dto) {
        return Result.success(supplierQualificationService.create(dto));
    }

    @Operation(summary = "更新资质")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "资质ID不能为空") Long id,
                               @Valid @RequestBody SupplierQualificationUpdateDTO dto) {
        supplierQualificationService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除资质")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable @NotNull(message = "资质ID不能为空") Long id) {
        supplierQualificationService.delete(id);
        return Result.success();
    }

    @Operation(summary = "上传资质证书附件")
    @PostMapping("/upload-attachment")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> uploadAttachment(@RequestParam("file") MultipartFile file,
                                         @RequestParam(required = false) Long qualificationId) throws IOException {
        return Result.success(fileAttachmentService.upload(file, "supplier_qualification", qualificationId, null));
    }
}