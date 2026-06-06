package com.supplier.system.service;

import com.supplier.system.entity.SysFileAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    SysFileAttachment store(MultipartFile file, String businessType, Long businessId, String businessNo) throws IOException;
    SysFileAttachment storeBytes(byte[] bytes, String fileName, String contentType, String businessType, Long businessId, String businessNo) throws IOException;
    byte[] load(SysFileAttachment attachment) throws IOException;
}
