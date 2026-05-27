package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ForecastDemandVO {

    private Long id;
    private String demandNo;
    private Long supplierId;
    private String materialCode;
    private LocalDate demandDate;
    private BigDecimal demandQty;
    private Integer demandType;
    private Integer demandStatus;
}