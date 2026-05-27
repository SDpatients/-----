package com.supplier.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageNoticeCreateDTO {
    private Long receiverUserId;
    private Long receiverSupplierId;
    private Integer channel = 1;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String businessType;
    private Long businessId;
}
