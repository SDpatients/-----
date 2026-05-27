package com.supplier.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("portal_todo")
public class PortalTodo extends BaseEntity {
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
}
