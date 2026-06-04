package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QualityInspectionVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String inspectionNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiptId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deliveryId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long standardId;
    private String materialCode;
    private String materialName;
    private BigDecimal inspectQty;
    private BigDecimal qualifiedQty;
    private BigDecimal unqualifiedQty;
    private Integer inspectResult;
    private Integer inspectType;
    private LocalDateTime inspectTime;
    private String inspectorName;
    private String inspectRemark;
    private Integer handleMethod;
    private String handleRemark;
    private InspectionStandardVO standard;
}
