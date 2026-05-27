package com.supplier.sourcing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RfqItemUpdateDTO {

    private Integer lineNo;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal quantity;
    private LocalDate targetDeliveryDate;
    private String remark;
}