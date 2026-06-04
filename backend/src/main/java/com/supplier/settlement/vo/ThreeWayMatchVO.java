package com.supplier.settlement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 三单匹配结果 VO
 */
@Data
public class ThreeWayMatchVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    private String orderNo;
    private BigDecimal orderAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiptId;
    private BigDecimal receiptAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long invoiceId;
    private String invoiceNo;
    private BigDecimal invoiceAmount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 0=完全匹配, 1=部分匹配, 2=不匹配 */
    private Integer matchResult;

    /** 匹配结果描述 */
    private String matchResultDesc;

    private BigDecimal diffAmount;
    private String diffReason;
    private LocalDateTime matchTime;
    private String remark;
}