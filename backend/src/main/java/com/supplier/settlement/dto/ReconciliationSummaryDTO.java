package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 对账汇总生成 DTO（联动6: 对账周期 → 自动汇总收货/退货/扣款）
 */
@Data
public class ReconciliationSummaryDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "对账周期不能为空")
    private String reconPeriod;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /** 是否包含扣款 */
    private Boolean includeDeduction = true;

    /** 备注 */
    private String remark;
}