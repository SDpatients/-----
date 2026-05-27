package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryPackageCreateDTO {
    @NotNull(message = "送货通知ID不能为空")
    private Long noticeId;
    @NotBlank(message = "箱号不能为空")
    private String packageNo;
    private String packageType;
    private BigDecimal weight;
    private BigDecimal volume;
    private String remark;
    private List<DeliveryPackageDetailItemDTO> details;
}