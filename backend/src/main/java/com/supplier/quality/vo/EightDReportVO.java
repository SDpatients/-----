package com.supplier.quality.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EightDReportVO {

    private Long id;
    private String reportNo;
    private Long ncrId;
    private Long supplierId;
    private String d1Team;
    private String d2Problem;
    private String d3Containment;
    private String d4RootCause;
    private String d5CorrectiveAction;
    private String d6ValidateAction;
    private String d7PreventAction;
    private String d8CloseSummary;
    private LocalDate dueDate;
    private Integer currentStep;
    private LocalDate stepDueDate;
    private Integer reportStatus;
    private LocalDateTime submitTime;
    private LocalDateTime auditTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
}