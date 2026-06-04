package com.supplier.sourcing.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SupplierQualificationUpdateDTO {

    private String qualType;
    private String qualName;
    private String qualNo;
    private String qualOrg;
    private LocalDate validStart;
    private LocalDate validEnd;
    private Long fileId;
    private Integer status;
    private Integer remindDays;
    private String remark;
}