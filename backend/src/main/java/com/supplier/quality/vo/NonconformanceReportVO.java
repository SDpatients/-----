package com.supplier.quality.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NonconformanceReportVO {

    private Long id;
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
    private LocalDateTime createTime;
}