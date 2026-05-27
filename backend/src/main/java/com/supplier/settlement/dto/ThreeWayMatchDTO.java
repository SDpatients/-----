package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 三单匹配 DTO（联动7: 三单匹配 → 自动匹配订单/收货/发票）
 */
@Data
public class ThreeWayMatchDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 订单号，可选，为空则匹配该供应商所有订单 */
    private String orderNo;

    /** 匹配日期范围开始 */
    private java.time.LocalDate startDate;

    /** 匹配日期范围结束 */
    private java.time.LocalDate endDate;

    /** 金额容差，默认0 */
    private BigDecimal tolerance = BigDecimal.ZERO;
}