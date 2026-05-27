package com.supplier.quality.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityAppealVO {

    private Long id;
    private String appealNo;
    private Long ncrId;
    private Long inspectionId;
    private Long supplierId;
    private String appealReason;
    private Integer appealStatus;
    private LocalDateTime submitTime;
    private Long auditBy;
    private LocalDateTime auditTime;
    private String auditRemark;
    private LocalDateTime createTime;
}