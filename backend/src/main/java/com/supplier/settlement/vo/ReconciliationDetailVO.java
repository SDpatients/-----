package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reconId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deliveryId;
    private String deliveryNo;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal orderAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal diffAmount;
    private String diffReason;
    private Integer confirmStatus;
    private LocalDateTime confirmTime;
    private String confirmRemark;
    private String remark;
}
