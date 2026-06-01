package com.supplier.sourcing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SupplierVO {

    private Long id;
    private String supplierCode;
    private String supplierName;
    private String supplierShortName;
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
    private Long auditBy;
    private String auditRemark;
    private String remark;
    private LocalDateTime createTime;

    private Boolean blacklisted;
    private String blacklistReason;
    private LocalDateTime blacklistStartTime;
    private LocalDateTime blacklistEndTime;
}