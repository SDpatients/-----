package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SupplierVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String supplierShortName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;
    private Integer supplierType;
    private String creditCode;
    private String legalPerson;
    private BigDecimal registeredCapital;
    private LocalDate establishDate;
    private String businessScope;
    private String province;
    private String city;
    private String district;
    private String address;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String bankName;
    private String bankAccount;
    private String taxNumber;
    private Integer rating;
    private Integer status;
    private LocalDateTime auditTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditBy;
    private String auditRemark;
    private String remark;
    private LocalDateTime createTime;

    private Long accountCount;

    private Boolean blacklisted;
    private String blacklistReason;
    private LocalDateTime blacklistStartTime;
    private LocalDateTime blacklistEndTime;
}