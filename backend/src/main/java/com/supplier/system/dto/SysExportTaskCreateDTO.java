package com.supplier.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysExportTaskCreateDTO {
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    private String exportParams;
    private Integer totalCount = 0;
}
