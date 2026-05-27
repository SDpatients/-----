package com.supplier.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DeliveryNoticeCreateDTO {
    @NotBlank(message = "送货通知单号不能为空")
    private String noticeNo;
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    private String orderNo;
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;
    private String supplierName;
    private LocalDate planDeliveryDate;
    private String deliveryMethod;
    private String deliveryCompany;
    private String deliveryNo;
    private String driverName;
    private String driverPhone;
    private String vehicleNo;
    private String deliveryAddress;
    private String receiver;
    private String receiverPhone;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String remark;
    /** ASN 明细行 */
    private List<DeliveryDetailItemDTO> details;
}
