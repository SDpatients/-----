package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceiptRecordVO {
    private Long id;
    private Long deliveryId;
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
