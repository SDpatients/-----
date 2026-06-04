package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierUpdateDTO {

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String supplierShortName;
    private Long categoryId;
    private Integer supplierType;
    private String creditCode;
    private String legalPerson;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String province;
    private String city;
    private String district;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNumber;
    private String remark;
}
