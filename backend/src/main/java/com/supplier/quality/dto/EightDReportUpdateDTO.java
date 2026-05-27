package com.supplier.quality.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EightDReportUpdateDTO {

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
}