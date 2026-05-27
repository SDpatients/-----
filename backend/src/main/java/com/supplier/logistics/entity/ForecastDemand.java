package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forecast_demand")
public class ForecastDemand extends BaseEntity {

    private String demandNo;
    private Long supplierId;
    private String materialCode;
    private LocalDate demandDate;
    private BigDecimal demandQty;
    private Integer demandType;
    private Integer demandStatus;
}