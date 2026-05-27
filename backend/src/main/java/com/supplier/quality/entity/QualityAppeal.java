package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quality_appeal")
public class QualityAppeal extends BaseEntity {

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
}