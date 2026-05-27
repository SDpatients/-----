package com.supplier.system.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.system.dto.SysExportTaskCreateDTO;
import com.supplier.system.query.SysExportTaskQuery;
import com.supplier.system.service.SysExportTaskService;
import com.supplier.system.vo.SysExportTaskVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.supplier.system.service.SysFileAttachmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/v1/export-tasks")
@RequiredArgsConstructor
public class SysExportTaskController {
    private final SysExportTaskService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SysExportTaskVO>> page(@Valid SysExportTaskQuery query) {
        return Result.success(service.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysExportTaskVO> detail(@PathVariable @NotNull(message = "导出任务ID不能为空") Long id) {
        return Result.success(service.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SysExportTaskCreateDTO dto) {
        return Result.success(service.create(dto));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable @NotNull(message = "导出任务ID不能为空") Long id) throws IOException {
        SysFileAttachmentService.FileDownload file = service.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.contentType()))
                .body(file.content());
    }
}
