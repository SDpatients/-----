package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptDiffVO {
    private Long id;
    private Long recordId;
    private Long noticeId;
    private String materialCode;
    private String materialName;
    private BigDecimal planQty;
    private BigDecimal receiptQty;
    private BigDecimal diffQty;
    private String diffReason;
    private Integer handleMethod;
    private String handleRemark;
    private Integer status;
    private String remark;
}