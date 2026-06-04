package com.supplier.sourcing.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SupplierQualificationVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String qualType;
    private String qualName;
    private String qualNo;
    private String qualOrg;
    private LocalDate validStart;
    private LocalDate validEnd;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;
    private Integer status;
    private Integer remindDays;
    private String remark;
    private LocalDateTime createTime;
}