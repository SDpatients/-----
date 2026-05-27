package com.supplier.sourcing.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierQualificationUpdateDTO {

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