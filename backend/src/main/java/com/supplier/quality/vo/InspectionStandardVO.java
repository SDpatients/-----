package com.supplier.quality.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InspectionStandardVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String standardNo;
    private String materialCode;
    private String materialName;
    private String standardName;
    private String sampleRule;
    private String versionNo;
    private Integer inspectionStrategy;
    private BigDecimal sampleRate;
    private BigDecimal acceptanceRate;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<InspectionStandardItemVO> items;
}