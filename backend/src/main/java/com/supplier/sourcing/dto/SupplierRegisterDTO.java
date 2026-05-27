package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商自助注册 DTO
 */
@Data
public class SupplierRegisterDTO {

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String supplierShortName;

    @NotBlank(message = "统一社会信用代码不能为空")
    private String creditCode;

    @NotBlank(message = "法定代表人不能为空")
    private String legalPerson;

    private String businessScope;

    private String province;
    private String city;
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @NotBlank(message = "联系邮箱不能为空")
    private String contactEmail;

    // 银行信息（复审需要）
    private String bankName;
    private String bankAccount;
    private String taxNumber;

    // 验证码（手机/邮箱验证）
    private String phoneVerifyCode;
    private String emailVerifyCode;

    private String remark;
}