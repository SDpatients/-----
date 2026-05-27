package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报价定价 DTO（联动1: RFQ定价 → 自动生成采购订单）
 */
@Data
public class QuotePriceDTO {

    /** 定价金额 */
    @NotNull(message = "定价金额不能为空")
    private BigDecimal awardAmount;

    /** 定价税额 */
    private BigDecimal awardTaxAmount;

    /** 定价币种 */
    private String awardCurrency;

    /** 期望交货日期 */
    private LocalDate deliveryDate;

    /** 付款条件 */
    private String paymentTerms;

    /** 交货地址 */
    private String deliveryAddress;

    /** 备注 */
    private String remark;
}