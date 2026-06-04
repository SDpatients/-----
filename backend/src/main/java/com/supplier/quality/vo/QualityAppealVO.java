package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityAppealVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String appealNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ncrId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String appealReason;
    private Integer appealStatus;
    private LocalDateTime submitTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditBy;
    private LocalDateTime auditTime;
    private String auditRemark;
    private LocalDateTime createTime;
}