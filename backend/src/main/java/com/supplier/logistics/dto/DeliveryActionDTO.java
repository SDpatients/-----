package com.supplier.logistics.dto;

import lombok.Data;

@Data
public class DeliveryActionDTO {
    private String remark;
    /** 收货仓库 */
    private String warehouse;
}
