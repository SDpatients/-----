package com.supplier.quality.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.quality.dto.EightDActionDTO;
import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.dto.EightDReportUpdateDTO;
import com.supplier.quality.query.EightDReportQuery;
import com.supplier.quality.service.EightDReportService;
import com.supplier.quality.vo.EightDReportVO;
import com.supplier.system.service.SysFileAttachmentService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Validated
@Tag(name = "8D整改报告")
@RestController
@RequestMapping("/v1/eight-d-reports")
@RequiredArgsConstructor
public class EightDReportController {

    private final EightDReportService eightDReportService;
    private final SysFileAttachmentService fileAttachmentService;

    @Operation(summary = "分页查询8D整改报告")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<EightDReportVO>> page(@Valid EightDReportQuery query) {
        return Result.success(eightDReportService.page(query));
    }

    @Operation(summary = "查询8D整改报告详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<EightDReportVO> detail(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id) {
        return Result.success(eightDReportService.getDetail(id));
    }

    @Operation(summary = "创建8D整改报告")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody EightDReportCreateDTO dto) {
        return Result.success(eightDReportService.create(dto));
    }

    @Operation(summary = "更新8D整改报告")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> update(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                               @Valid @RequestBody EightDReportUpdateDTO dto) {
        eightDReportService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "提交8D整改报告")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                               @RequestBody EightDActionDTO dto) {
        eightDReportService.submit(id, dto);
        return Result.success();
    }

    @Operation(summary = "审核8D整改报告")
    @PostMapping("/{id}/audit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> audit(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                              @RequestBody EightDActionDTO dto) {
        eightDReportService.audit(id, dto);
        return Result.success();
    }

    @Operation(summary = "退回8D整改报告")
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> reject(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                               @RequestBody EightDActionDTO dto) {
        eightDReportService.reject(id, dto);
        return Result.success();
    }

    @Operation(summary = "关闭8D整改报告")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                              @RequestBody EightDActionDTO dto) {
        eightDReportService.close(id, dto);
        return Result.success();
    }

    @Operation(summary = "提交8D阶段(D1-D8)")
    @PostMapping("/{id}/step-submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> stepSubmit(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                                   @RequestBody EightDActionDTO dto) {
        eightDReportService.stepSubmit(id, dto);
        return Result.success();
    }

    @Operation(summary = "审核8D阶段(推进到下一阶段)")
    @PostMapping("/{id}/step-approve")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> stepApprove(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                                    @RequestBody EightDActionDTO dto) {
        eightDReportService.stepApprove(id, dto);
        return Result.success();
    }

    @Operation(summary = "上传8D报告附件")
    @PostMapping("/{id}/upload-attachment")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> uploadAttachment(@PathVariable @NotNull(message = "8D报告ID不能为空") Long id,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(fileAttachmentService.upload(file, "8d_report", id, null));
    }
}