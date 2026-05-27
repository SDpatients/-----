package com.supplier.quality.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QualityInspectionVO {
    private Long id;
    private String inspectionNo;
    private Long receiptId;
    private Long deliveryId;
    private Long supplierId;
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
