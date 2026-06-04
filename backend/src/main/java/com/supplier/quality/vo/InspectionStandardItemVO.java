package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InspectionStandardItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
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