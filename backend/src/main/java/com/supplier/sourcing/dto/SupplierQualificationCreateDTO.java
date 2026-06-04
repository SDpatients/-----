package com.supplier.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SupplierQualificationCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "资质类型不能为空")
    private String qualType;

    @NotBlank(message = "资质名称不能为空")
    private String qualName;

    private String qualNo;
    private String qualOrg;
    private LocalDate validStart;
    private LocalDate validEnd;
    private Long fileId;
    private Integer remindDays;
    private String remark;
}