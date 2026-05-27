package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuoteCompareVO {

    private Long rfqId;
    private String rfqNo;
    private String rfqTitle;
    private List<MaterialCompare> materialCompares;

    @Data
    public static class MaterialCompare {
        private Long rfqItemId;
        private String materialCode;
        private String materialName;
        private String materialSpec;
        private BigDecimal quantity;
        private List<SupplierQuoteInfo> supplierQuotes;
    }

    @Data
    public static class SupplierQuoteInfo {
        private Long quoteId;
        private String quoteNo;
        private Long supplierId;
        private String supplierName;
        private BigDecimal price;
        private BigDecimal taxPrice;
        private BigDecimal taxRate;
        private BigDecimal amount;
        private BigDecimal taxAmount;
        private Integer deliveryDays;
        private String paymentTerms;
        private BigDecimal supplierPerformanceScore;
    }
}