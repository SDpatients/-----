package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 申诉 VO
 */
@Data
public class AppealVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String appealNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ncrId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deductionId;
    @JsonSerialize(using = ToStringSerializer.class)
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