package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_barcode")
public class DeliveryBarcode extends BaseEntity {
    private Long noticeId;
    private Long packageId;
    private String barcode;
    private String barcodeType;
    private Integer printCount;
    private String remark;
}