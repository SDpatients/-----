package com.supplier.sourcing.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierBlacklistUpdateDTO {

    private String reason;
    private LocalDateTime endTime;
}