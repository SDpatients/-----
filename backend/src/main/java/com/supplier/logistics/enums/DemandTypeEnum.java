package com.supplier.logistics.enums;

import lombok.Getter;

@Getter
public enum DemandTypeEnum {

    FORECAST(1, "预测"),
    JIT(2, "JIT"),
    VMI_REPLENISH(3, "VMI补货");

    private final Integer code;
    private final String desc;

    DemandTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}