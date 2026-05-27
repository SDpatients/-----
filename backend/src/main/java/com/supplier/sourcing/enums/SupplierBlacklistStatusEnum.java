package com.supplier.sourcing.enums;

import lombok.Getter;

@Getter
public enum SupplierBlacklistStatusEnum {

    RESOLVED(0, "已解除"),
    ACTIVE(1, "生效中");

    private final Integer code;
    private final String desc;

    SupplierBlacklistStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}