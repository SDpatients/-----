package com.supplier.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysExportTaskVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String taskNo;
    private String taskType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;
    private String exportParams;
    private Integer totalCount;
    private Integer processedCount;
    private Integer taskStatus;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
