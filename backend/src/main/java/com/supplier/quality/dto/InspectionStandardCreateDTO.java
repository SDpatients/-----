package com.supplier.quality.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionStandardCreateDTO {
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String standardName;
    private String sampleRule;
    private String versionNo;
    /** 检验策略: 0-免检, 1-抽检, 2-全检 */
    private Integer inspectionStrategy;
    /** 抽检比例 (如 0.1 表示 10%) */
    private BigDecimal sampleRate;
    /** 合格标准 (如 97.0 表示 97%) */
    private BigDecimal acceptanceRate;
    private String remark;
}