package com.supplier.sourcing.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqUpdateDTO {

    private String rfqTitle;
    private Long orgId;
    private String currency;
    private LocalDateTime quoteDeadline;
    private String remark;
}