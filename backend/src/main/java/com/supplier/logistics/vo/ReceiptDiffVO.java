package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiptDiffVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;
    @JsonSerialize(using = ToStringSerializer.class)
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