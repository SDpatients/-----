package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nonconformance_report")
public class NonconformanceReport extends BaseEntity {

    private String ncrNo;
    private Long inspectionId;
    private Long receiptId;
    private Long supplierId;
    private String materialCode;
    private String materialName;
    private BigDecimal unqualifiedQty;
    private String problemDesc;
    private Integer severity;
    private Integer handleMethod;
    private String handleDetail;
    private String handleRemark;
    private Integer ncrStatus;
    private LocalDateTime submitTime;
    private LocalDateTime closeTime;
    private String closeRemark;
}