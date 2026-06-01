package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_info")
public class SupplierInfo extends BaseEntity {

    private Long tenantId;
    private Long orgId;
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
    private String invoiceAddress;
    private String invoicePhone;
    private Integer rating;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime auditTime;
    private Long auditBy;
    private String auditRemark;
    private String remark;
}
