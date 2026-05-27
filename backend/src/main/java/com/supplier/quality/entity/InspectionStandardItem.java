package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_standard_item")
public class InspectionStandardItem extends BaseEntity {

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