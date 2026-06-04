package com.supplier.portal.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalTodoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    private String todoType;
    private String businessType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private String businessNo;
    private String title;
    private Integer todoStatus;
    private LocalDateTime dueTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
}
