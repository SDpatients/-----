package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class DeliveryBarcodeVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;
    private String barcode;
    private String barcodeType;
    private Integer printCount;
    private String remark;
}