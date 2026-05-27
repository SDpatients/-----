package com.supplier.quality.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InspectionStandardItemVO {

    private Long id;
    private Long standardId;
    private String itemName;
    private Integer itemType;
    private String standardValue;
    private BigDecimal upperLimit;
    private BigDecimal lowerLimit;
    private String unit;
    private Integer required;
    private Integer sort;
}