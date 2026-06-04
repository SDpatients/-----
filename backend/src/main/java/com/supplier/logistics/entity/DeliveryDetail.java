package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_detail")
public class DeliveryDetail extends BaseEntity {
    private Long noticeId;
    private Long orderDetailId;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal planQty;
    private BigDecimal actualQty;
    private BigDecimal receivedQty;
    private BigDecimal qualifiedQty;
    private String batchNo;
    private LocalDate productionDate;
    private Integer boxCount;
    private String caseNo;
    private Integer qtyPerCase;
    private String barcode;
    private LocalDate expiryDate;
    private String remark;
}
