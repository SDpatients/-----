package com.supplier.common.event;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_event_log")
public class MessageEventLog extends BaseEntity {
    private String eventId;
    private String eventType;
    private String source;
    private String traceId;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private String payload;
    private Integer publishStatus;
    private Integer consumeStatus;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime occurredAt;
}