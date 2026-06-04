package com.supplier.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportCheckResultVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;
    private String importType;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long errorFileId;
}
