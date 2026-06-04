package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ForecastDemandVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String demandNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String materialCode;
    private LocalDate demandDate;
    private BigDecimal demandQty;
    private Integer demandType;
    private Integer demandStatus;
}