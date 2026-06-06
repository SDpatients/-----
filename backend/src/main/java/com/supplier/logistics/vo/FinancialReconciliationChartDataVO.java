package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinancialReconciliationChartDataVO {
    private List<TrendItem> trends;
    private List<SupplierDistributionItem> supplierDistribution;
    private List<StatusDistributionItem> paymentStatus;
    private List<StatusDistributionItem> reconciliationStatus;

    @Data
    public static class TrendItem {
        private String period;
        private BigDecimal totalAmount;
        private BigDecimal reconciledAmount;
        private BigDecimal diffAmount;
        private Integer count;
    }

    @Data
    public static class SupplierDistributionItem {
        private String supplierName;
        private BigDecimal amount;
        private Integer count;
    }

    @Data
    public static class StatusDistributionItem {
        private String status;
        private String label;
        private Integer count;
        private BigDecimal amount;
    }
}
