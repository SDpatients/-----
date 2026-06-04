package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceiptRecordVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deliveryId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    private String materialCode;
    private String materialName;
    private BigDecimal planQty;
    private BigDecimal receiptQty;
    private BigDecimal rejectQty;
    private LocalDateTime receiptTime;
    private String receiverName;
    private String warehouseName;
    private String location;
    private Integer receiptStatus;
    private String rejectReason;
    private String remark;
}
