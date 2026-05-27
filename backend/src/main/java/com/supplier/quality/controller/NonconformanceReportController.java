package com.supplier.quality.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.quality.dto.NcrActionDTO;
import com.supplier.quality.dto.NonconformanceReportCreateDTO;
import com.supplier.quality.query.NonconformanceReportQuery;
import com.supplier.quality.service.NonconformanceReportService;
import com.supplier.quality.vo.NonconformanceReportVO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Validated
@Tag(name = "NCR质量异常")
@RestController
@RequestMapping("/v1/nonconformance-reports")
@RequiredArgsConstructor
public class NonconformanceReportController {

    private final NonconformanceReportService nonconformanceReportService;
    private final SysFileAttachmentService fileAttachmentService;

    @Operation(summary = "分页查询NCR质量异常")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<NonconformanceReportVO>> page(@Valid NonconformanceReportQuery query) {
        return Result.success(nonconformanceReportService.page(query));
    }

    @Operation(summary = "查询NCR质量异常详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<NonconformanceReportVO> detail(@PathVariable @NotNull(message = "NCR ID不能为空") Long id) {
        return Result.success(nonconformanceReportService.getDetail(id));
    }

    @Operation(summary = "创建NCR质量异常")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody NonconformanceReportCreateDTO dto) {
        return Result.success(nonconformanceReportService.create(dto));
    }

    @Operation(summary = "提交NCR")
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submit(@PathVariable @NotNull(message = "NCR ID不能为空") Long id,
                               @RequestBody NcrActionDTO dto) {
        nonconformanceReportService.submit(id, dto);
        return Result.success();
    }

    @Operation(summary = "处理NCR")
    @PostMapping("/{id}/handle")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> handle(@PathVariable @NotNull(message = "NCR ID不能为空") Long id,
                               @RequestBody NcrActionDTO dto) {
        nonconformanceReportService.handle(id, dto);
        return Result.success();
    }

    @Operation(summary = "验证NCR")
    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> verify(@PathVariable @NotNull(message = "NCR ID不能为空") Long id,
                               @RequestBody NcrActionDTO dto) {
        nonconformanceReportService.verify(id, dto);
        return Result.success();
    }

    @Operation(summary = "关闭NCR")
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> close(@PathVariable @NotNull(message = "NCR ID不能为空") Long id,
                              @RequestBody NcrActionDTO dto) {
        nonconformanceReportService.close(id, dto);
        return Result.success();
    }

    @Operation(summary = "上传NCR附件")
    @PostMapping("/{id}/upload-attachment")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> uploadAttachment(@PathVariable @NotNull(message = "NCR ID不能为空") Long id,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(fileAttachmentService.upload(file, "ncr", id, null));
    }
}