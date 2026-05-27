package com.supplier.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_notice")
public class MessageNotice extends BaseEntity {
    private String noticeNo;
    private Long receiverUserId;
    private Long receiverSupplierId;
    private Integer channel;
    private String title;
    private String content;
    private String businessType;
    private Long businessId;
    private Integer sendStatus;
    private Integer readStatus;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private Integer retryCount;
    private String errorMessage;
}
