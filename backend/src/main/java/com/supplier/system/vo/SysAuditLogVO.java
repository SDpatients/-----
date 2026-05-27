package com.supplier.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysAuditLogVO {
    private Long id;
    private String traceId;
    private Long userId;
    private String username;
    private String moduleName;
    private String businessType;
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
