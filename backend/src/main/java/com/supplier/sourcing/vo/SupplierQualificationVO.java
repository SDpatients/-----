package com.supplier.sourcing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierQualificationVO {

    private Long id;
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
    private LocalDateTime createTime;
}