package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_contact")
public class SupplierContact extends BaseEntity {

    private Long supplierId;
    private String contactName;
    private String contactRole;
    private String contactPhone;
    private String contactEmail;
    private Integer isPrimary;
    private Integer status;
    private String remark;
}