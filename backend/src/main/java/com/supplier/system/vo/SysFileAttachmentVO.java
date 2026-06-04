package com.supplier.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysFileAttachmentVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String businessType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private String businessNo;
    private String fileName;
    private String fileExt;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileSize;
    private String contentType;
    private String bucketName;
    private String objectKey;
    private String fileHash;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadUserId;
    private String uploadUserName;
    private LocalDateTime uploadTime;
    private Integer status;
}
