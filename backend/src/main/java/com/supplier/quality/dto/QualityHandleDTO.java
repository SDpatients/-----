package com.supplier.quality.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QualityHandleDTO {
    @NotNull(message = "处理方式不能为空")
    private Integer handleMethod;
    private String handleRemark;
}
