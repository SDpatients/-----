package com.supplier.logistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeliveryDetailItemDTO {
    @NotNull(message = "订单明细ID不能为空")
    @JsonProperty("orderDetailId")
    private Long orderDetailId;

    @JsonProperty("orderLineNo")
    private Long orderLineNo;

    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    private String materialSpec;
    private String unit;

    @JsonProperty("shipQty")
    private BigDecimal actualQty = BigDecimal.ZERO;

    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;

    @JsonProperty("lineNo")
    private Integer lineNo;

    @JsonProperty("caseNo")
    private String caseNo;

    @JsonProperty("qtyPerCase")
    private Integer qtyPerCase;

    @JsonProperty("barcode")
    private String barcode;

    private String remark;
}