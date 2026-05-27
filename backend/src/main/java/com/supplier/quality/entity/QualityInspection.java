package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quality_inspection")
public class QualityInspection extends BaseEntity {
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
    private Long inspector;
    private String inspectorName;
    private String inspectRemark;
    private Integer handleMethod;
    private String handleRemark;
}
