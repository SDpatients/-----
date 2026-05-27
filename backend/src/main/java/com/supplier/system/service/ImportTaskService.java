package com.supplier.system.service;

import com.supplier.system.vo.ImportCheckResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportTaskService {
    SysFileAttachmentService.FileDownload downloadTemplate(String importType);
    ImportCheckResultVO check(String importType, MultipartFile file) throws IOException;
    Long submit(String importType, Long fileId);
    SysFileAttachmentService.FileDownload downloadErrors(Long fileId) throws IOException;
}
