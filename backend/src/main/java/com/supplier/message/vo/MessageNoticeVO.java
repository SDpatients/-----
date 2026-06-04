package com.supplier.message.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageNoticeVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String noticeNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverUserId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverSupplierId;
    private Integer channel;
    private String title;
    private String content;
    private String businessType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    private Integer sendStatus;
    private Integer readStatus;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private Integer retryCount;
    private String errorMessage;
}
