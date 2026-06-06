package com.supplier.system.service.impl;

import com.supplier.security.util.SecurityUtils;
import com.supplier.system.entity.SysFileAttachment;
import com.supplier.system.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {
    @Value("${supplier.file-storage.root:uploads}")
    private String root;

    @Override
    public SysFileAttachment store(MultipartFile file, String businessType, Long businessId, String businessNo) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1) : "";
        LocalDate date = LocalDate.now();
        String objectKey = businessType + "/" + date.getYear() + "/" + String.format("%02d", date.getMonthValue()) + "/" + String.format("%02d", date.getDayOfMonth()) + "/" + (businessNo == null ? "general" : businessNo) + "/" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
        Path target = Path.of(root, objectKey);
        Files.createDirectories(target.getParent());
        byte[] bytes = file.getBytes();
        Files.write(target, bytes);
        SysFileAttachment attachment = new SysFileAttachment();
        attachment.setBusinessType(businessType);
        attachment.setBusinessId(businessId);
        attachment.setBusinessNo(businessNo);
        attachment.setFileName(originalName);
        attachment.setFileExt(ext);
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setBucketName("local");
        attachment.setObjectKey(objectKey.replace("\\", "/"));
        attachment.setFileHash(sha256(bytes));
        attachment.setUploadUserId(SecurityUtils.getUserId());
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setStatus(1);
        return attachment;
    }

    @Override
    public byte[] load(SysFileAttachment attachment) throws IOException {
        return Files.readAllBytes(Path.of(root, attachment.getObjectKey()));
    }

    @Override
    public SysFileAttachment storeBytes(byte[] bytes, String fileName, String contentType, String businessType, Long businessId, String businessNo) throws IOException {
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        LocalDate date = LocalDate.now();
        String objectKey = businessType + "/" + date.getYear() + "/" + String.format("%02d", date.getMonthValue()) + "/" + String.format("%02d", date.getDayOfMonth()) + "/" + (businessNo == null ? "general" : businessNo) + "/" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
        Path target = Path.of(root, objectKey);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        SysFileAttachment attachment = new SysFileAttachment();
        attachment.setBusinessType(businessType);
        attachment.setBusinessId(businessId);
        attachment.setBusinessNo(businessNo);
        attachment.setFileName(fileName);
        attachment.setFileExt(ext);
        attachment.setFileSize((long) bytes.length);
        attachment.setContentType(contentType);
        attachment.setBucketName("local");
        attachment.setObjectKey(objectKey.replace("\\", "/"));
        attachment.setFileHash(sha256(bytes));
        attachment.setUploadUserId(SecurityUtils.getUserId());
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setStatus(1);
        return attachment;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
