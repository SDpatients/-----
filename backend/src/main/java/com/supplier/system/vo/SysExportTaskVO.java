package com.supplier.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysExportTaskVO {
    private Long id;
    private String taskNo;
    private String taskType;
    private Long fileId;
    private String exportParams;
    private Integer totalCount;
    private Integer processedCount;
    private Integer taskStatus;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
