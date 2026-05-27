package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_standard")
public class InspectionStandard extends BaseEntity {
    private String standardNo;
    private String materialCode;
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
    private Integer status;
    private String remark;
}