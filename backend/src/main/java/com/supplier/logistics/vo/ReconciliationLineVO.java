package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 对账明细行 —— 单条物料的对账信息
 */
@Data
public class ReconciliationLineVO {
    /** delivery_detail.id */
    private Long detailId;
    /** purchase_order_detail.id */
    private Long orderDetailId;
    /** 物料编码 */
    private String materialCode;
    /** 物料名称 */
    private String materialName;
    /** 规格 */
    private String materialSpec;
    /** 单位 */
    private String unit;
    /** 采购订单数量 */
    private BigDecimal orderQty;
    /** 采购单价(含税) */
    private BigDecimal unitPrice;
    /** 采购金额(含税) = orderQty * unitPrice */
    private BigDecimal orderAmount;
    /** 实际发货数量 */
    private BigDecimal deliveryQty;
    /** 实收数量 */
    private BigDecimal receivedQty;
    /** 对账数量（默认=实收数量，可编辑） */
    private BigDecimal reconcileQty;
    /** 对账金额 = reconcileQty * unitPrice */
    private BigDecimal reconcileAmount;
    /** 差异数量 = reconcileQty - orderQty */
    private BigDecimal diffQty;
    /** 差异金额 = reconcileAmount - orderAmount */
    private BigDecimal diffAmount;
}
