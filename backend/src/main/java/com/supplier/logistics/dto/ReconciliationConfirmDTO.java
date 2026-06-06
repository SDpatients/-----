package com.supplier.logistics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 对账确认提交DTO
 */
@Data
public class ReconciliationConfirmDTO {
    /** 对账状态: 2-已对账 3-有差异 */
    @NotNull(message = "对账状态不能为空")
    private Integer reconciliationStatus;

    /** 差异原因（状态为有差异时必填） */
    private String diffReason;

    /** 各行对账数量 */
    @Valid
    private List<LineReconcileItem> lines;

    @Data
    public static class LineReconcileItem {
        /** delivery_detail.id */
        @NotNull(message = "明细ID不能为空")
        private Long detailId;
        /** 对账数量 */
        @NotNull(message = "对账数量不能为空")
        private BigDecimal reconcileQty;
    }
}
