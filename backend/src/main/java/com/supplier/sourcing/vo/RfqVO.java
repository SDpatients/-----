package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqVO {

    private Long id;
    private String rfqNo;
    private String rfqTitle;
    private Long orgId;
    private String currency;
    private LocalDateTime quoteDeadline;
    private Integer rfqStatus;
    private LocalDateTime publishTime;
    private LocalDateTime closeTime;
    private String remark;
    private LocalDateTime createTime;
}