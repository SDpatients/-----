package com.supplier.quality.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InspectionStandardUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    private String materialCode;
    private String materialName;
    private String standardName;
    private String sampleRule;
    private String versionNo;
    private Integer status;
    private String remark;
}