package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("receipt_record")
public class ReceiptRecord extends BaseEntity {
    private String receiptNo;
    private Long deliveryId;
    private Long noticeId;
    private Long supplierId;
    private String materialCode;
    private String materialName;
    private BigDecimal planQty;
    private BigDecimal receiptQty;
    private BigDecimal rejectQty;
    private LocalDateTime receiptTime;
    private Long receiver;
    private String receiverName;
    private Long warehouseId;
    private String warehouseName;
    private String location;
    private Integer receiptStatus;
    private String diffReason;
    private String rejectReason;
    private String remark;
}
