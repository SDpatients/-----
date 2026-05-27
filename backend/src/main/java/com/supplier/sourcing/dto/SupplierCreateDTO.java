package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateDTO {

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

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
    private String address;
    private String remark;
}
