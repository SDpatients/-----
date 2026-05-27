package com.supplier.portal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalTodoVO {
    private Long id;
    private Long userId;
    private Long supplierId;
    private String todoType;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private String title;
    private Integer todoStatus;
    private LocalDateTime dueTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
}
