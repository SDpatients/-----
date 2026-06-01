package com.supplier.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order_detail")
public class PurchaseOrderDetail extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Integer lineNo;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String materialModel;
    private String unit;
    private BigDecimal quantity;
    @TableField("price")
    private BigDecimal unitPrice;
    @TableField("tax_price")
    private BigDecimal taxPrice;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amount;
    private BigDecimal deliveredQty;
    private BigDecimal receivedQty;
    private BigDecimal qualifiedQty;
    private LocalDate deliveryDate;
    private LocalDate promiseDate;
    private Integer lineStatus;
    private String remark;
}
