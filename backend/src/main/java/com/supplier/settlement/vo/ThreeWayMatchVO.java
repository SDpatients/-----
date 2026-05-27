package com.supplier.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 三单匹配结果 VO
 */
@Data
public class ThreeWayMatchVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private BigDecimal orderAmount;
    private Long receiptId;
    private BigDecimal receiptAmount;
    private Long invoiceId;
    private String invoiceNo;
    private BigDecimal invoiceAmount;
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