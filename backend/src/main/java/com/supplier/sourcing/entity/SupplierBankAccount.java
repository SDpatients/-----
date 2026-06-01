package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_bank_account")
public class SupplierBankAccount extends BaseEntity {

    private Long supplierId;
    private String accountName;
    private String bankName;
    private String bankBranch;
    private String bankAccount;
    private String currency;
    private Integer isDefault;
    private Integer status;
}