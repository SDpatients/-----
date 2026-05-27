package com.supplier.message.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageNoticeVO {
    private Long id;
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
