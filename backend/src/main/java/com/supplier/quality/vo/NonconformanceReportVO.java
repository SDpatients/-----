package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NonconformanceReportVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String ncrNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiptId;
    @JsonSerialize(using = ToStringSerializer.class)
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