package com.supplier.quality.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EightDReportCreateDTO {

    @NotNull(message = "NCR ID不能为空")
    private Long ncrId;

    @NotNull(message = "供应商ID不能为空")
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
}