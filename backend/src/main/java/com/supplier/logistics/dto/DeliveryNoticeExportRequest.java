package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * ASN 导出请求 DTO
 */
@Data
public class DeliveryNoticeExportRequest {

    @NotBlank(message = "导出范围不能为空")
    private String scope; // current / all / selected

    private List<Long> selectedIds;

    // 复用查询条件
    private String keyword;
    private Long supplierId;
    private Integer deliveryStatus;
    private String startDate;
    private String endDate;
}
