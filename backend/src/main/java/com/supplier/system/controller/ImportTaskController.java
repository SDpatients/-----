package com.supplier.system.controller;

import com.supplier.common.result.Result;
import com.supplier.system.service.ImportTaskService;
import com.supplier.system.service.SysFileAttachmentService;
import com.supplier.system.vo.ImportCheckResultVO;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/v1/imports")
@RequiredArgsConstructor
public class ImportTaskController {
    private final ImportTaskService service;

    @GetMapping("/{importType}/template")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> template(@PathVariable @NotBlank String importType) {
        SysFileAttachmentService.FileDownload file = service.downloadTemplate(importType);
        return response(file);
    }

    @PostMapping("/{importType}/check")
    @PreAuthorize("isAuthenticated()")
    public Result<ImportCheckResultVO> check(@PathVariable @NotBlank String importType, @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(service.check(importType, file));
    }

    @PostMapping("/{importType}/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> submit(@PathVariable @NotBlank String importType, @RequestParam @NotNull Long fileId) {
        return Result.success(service.submit(importType, fileId));
    }

    @GetMapping("/errors/{fileId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadErrors(@PathVariable @NotNull Long fileId) throws IOException {
        return response(service.downloadErrors(fileId));
    }

    private ResponseEntity<byte[]> response(SysFileAttachmentService.FileDownload file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.contentType()))
                .body(file.content());
    }
}
