package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rfq")
public class Rfq extends BaseEntity {

    private String rfqNo;
    private String rfqTitle;
    private Long orgId;
    private String currency;
    private LocalDateTime quoteDeadline;
    private Integer rfqStatus;
    private LocalDateTime publishTime;
    private LocalDateTime closeTime;
    private String remark;
}