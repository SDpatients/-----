package com.supplier.logistics.enums;

import lombok.Getter;

@Getter
public enum InventoryStatusEnum {

    NORMAL(1, "正常"),
    WARNING(2, "预警"),
    SHORTAGE(3, "缺料");

    private final Integer code;
    private final String desc;

    InventoryStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}