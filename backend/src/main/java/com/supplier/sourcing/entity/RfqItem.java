package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rfq_item")
public class RfqItem extends BaseEntity {

    private Long rfqId;
    private Integer lineNo;
    private String materialCode;
    private String materialName;
    private String materialSpec;
    private String unit;
    private BigDecimal quantity;
    private LocalDate targetDeliveryDate;
    private String remark;
}