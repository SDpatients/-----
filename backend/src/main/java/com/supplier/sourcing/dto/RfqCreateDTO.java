package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RfqCreateDTO {

    @NotBlank(message = "询价单号不能为空")
    private String rfqNo;

    @NotBlank(message = "询价标题不能为空")
    private String rfqTitle;

    private Long orgId;
    private String currency;
    private LocalDateTime quoteDeadline;
    private String remark;
}