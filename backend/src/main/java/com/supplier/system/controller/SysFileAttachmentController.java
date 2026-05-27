package com.supplier.system.controller;

import com.supplier.common.result.PageResult;
import com.supplier.common.result.Result;
import com.supplier.system.dto.SysFileAttachmentCreateDTO;
import com.supplier.system.query.SysFileAttachmentQuery;
import com.supplier.system.service.SysFileAttachmentService;
import com.supplier.system.vo.SysFileAttachmentVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/v1/file-attachments")
@RequiredArgsConstructor
public class SysFileAttachmentController {
    private final SysFileAttachmentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<SysFileAttachmentVO>> page(@Valid SysFileAttachmentQuery query) {
        return Result.success(service.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysFileAttachmentVO> detail(@PathVariable @NotNull(message = "附件ID不能为空") Long id) {
        return Result.success(service.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Long> create(@Valid @RequestBody SysFileAttachmentCreateDTO dto) {
        return Result.success(service.create(dto));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                               @RequestParam String businessType,
                               @RequestParam(required = false) Long businessId,
                               @RequestParam(required = false) String businessNo) throws IOException {
        return Result.success(service.upload(file, businessType, businessId, businessNo));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable @NotNull(message = "附件ID不能为空") Long id) throws IOException {
        SysFileAttachmentService.FileDownload file = service.load(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.contentType()))
                .body(file.content());
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> preview(@PathVariable @NotNull(message = "附件ID不能为空") Long id) throws IOException {
        SysFileAttachmentService.FileDownload file = service.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.contentType()))
                .body(file.content());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> disable(@PathVariable @NotNull(message = "附件ID不能为空") Long id) {
        service.disable(id);
        return Result.success();
    }
}
