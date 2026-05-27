package com.supplier.quality.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 申诉 VO
 */
@Data
public class AppealVO {

    private Long id;
    private String appealNo;
    private Long ncrId;
    private Long inspectionId;
    private Long deductionId;
    private Long supplierId;
    private String materialCode;
    private String materialName;
    private String appealReason;
    private String appealDesc;
    private BigDecimal adjustAmount;
    private Integer appealStatus;
    private String appealStatusDesc;
    private LocalDateTime submitTime;
    private String reviewerName;
    private LocalDateTime reviewTime;
    private String reviewOpinion;
    private String remark;
    private LocalDateTime createTime;
}