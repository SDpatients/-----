package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_attachment")
public class SysFileAttachment extends BaseEntity {
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
