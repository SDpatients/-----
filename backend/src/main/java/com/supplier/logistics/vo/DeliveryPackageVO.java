package com.supplier.logistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryPackageVO {
    private Long id;
    private Long noticeId;
    private String packageNo;
    private String packageType;
    private BigDecimal weight;
    private BigDecimal volume;
    private String remark;
    private List<DeliveryPackageDetailVO> details;
    private List<DeliveryBarcodeVO> barcodes;
}