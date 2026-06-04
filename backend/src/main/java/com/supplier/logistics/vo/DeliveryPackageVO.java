package com.supplier.logistics.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryPackageVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    private String packageNo;
    private String packageType;
    private BigDecimal weight;
    private BigDecimal volume;
    private String remark;
    private List<DeliveryPackageDetailVO> details;
    private List<DeliveryBarcodeVO> barcodes;
}