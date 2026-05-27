package com.supplier.logistics.vo;

import lombok.Data;

@Data
public class DeliveryBarcodeVO {
    private Long id;
    private Long noticeId;
    private Long packageId;
    private String barcode;
    private String barcodeType;
    private Integer printCount;
    private String remark;
}