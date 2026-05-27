package com.supplier.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalTodoCreateDTO {
    private Long userId;
    private Long supplierId;
    @NotBlank(message = "待办类型不能为空")
    private String todoType;
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    @NotNull(message = "业务ID不能为空")
    private Long businessId;
    private String businessNo;
    @NotBlank(message = "标题不能为空")
    private String title;
    private LocalDateTime dueTime;
}