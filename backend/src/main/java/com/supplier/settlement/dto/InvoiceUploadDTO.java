package com.supplier.settlement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceUploadDTO {

    @NotNull(message = "附件ID不能为空")
    private Long fileId;
}