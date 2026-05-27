package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QualityAppealAuditDTO {

    @NotBlank(message = "审核意见不能为空")
    private String auditRemark;
}