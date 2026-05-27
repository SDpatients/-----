package com.supplier.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity {
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
