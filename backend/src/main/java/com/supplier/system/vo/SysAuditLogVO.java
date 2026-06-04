package com.supplier.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysAuditLogVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String traceId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String moduleName;
    private String businessType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private String businessNo;
    private String actionName;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String requestMethod;
    private String requestPath;
    private String clientIp;
    private Integer resultStatus;
    private String errorMessage;
    private LocalDateTime operateTime;
}
