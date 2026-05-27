package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RfqItemVO {

    private Long id;
    private Long rfqId;
    private Integer lineNo;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal quantity;
    private LocalDate targetDeliveryDate;
    private String remark;
    private LocalDateTime createTime;
}