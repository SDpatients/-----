package com.supplier.sourcing.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RfqItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rfqId;
    private Integer lineNo;
    private String materialCode;
    private String materialName;
    @JsonProperty("spec")
    private String materialSpec;
    private String unit;
    private BigDecimal quantity;
    @JsonProperty("deliveryDate")
    private LocalDate targetDeliveryDate;
    private String remark;
    private LocalDateTime createTime;
}