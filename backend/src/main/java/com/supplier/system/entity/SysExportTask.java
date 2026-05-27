package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_export_task")
public class SysExportTask extends BaseEntity {
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
