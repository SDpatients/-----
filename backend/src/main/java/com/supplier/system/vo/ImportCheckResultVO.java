package com.supplier.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportCheckResultVO {
    private Long fileId;
    private String importType;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private Long errorFileId;
}
