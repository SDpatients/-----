package com.supplier.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysFileAttachmentVO {
    private Long id;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String contentType;
    private String bucketName;
    private String objectKey;
    private String fileHash;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
    private Integer status;
}
