package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("write_off")
public class WriteOff extends BaseEntity {
    private String writeOffNo;
    private Long noticeId;
    private String asnNo;
    private Long orderId;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private String writeOffType;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime writeOffTime;
    private String remark;
}
