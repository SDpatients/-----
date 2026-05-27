package com.supplier.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysFileAttachmentCreateDTO {
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    private Long businessId;
    private String businessNo;
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    private String fileExt;
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
    private String contentType;
    @NotBlank(message = "存储桶不能为空")
    private String bucketName;
    @NotBlank(message = "对象Key不能为空")
    private String objectKey;
    private String fileHash;
}
