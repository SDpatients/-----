package com.supplier.sourcing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_qualification")
public class SupplierQualification extends BaseEntity {

    private Long supplierId;
    private String qualType;
    private String qualName;
    private String qualNo;
    private String qualOrg;
    private LocalDateTime validStart;
    private LocalDateTime validEnd;
    private Long fileId;
    private Integer status;
    private Integer remindDays;
    private String remark;
}