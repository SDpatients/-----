package com.supplier.quality.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EightDActionDTO {

    private String remark;
    private Integer currentStep;
    private LocalDate stepDueDate;
    /**
     * 阶段内容：D1团队/D2问题/D3遏制/D4根因/D5纠正/D6验证/D7预防/D8结案
     */
    private String stepContent;
}