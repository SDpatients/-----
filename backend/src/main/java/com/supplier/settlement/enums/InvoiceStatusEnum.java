package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum InvoiceStatusEnum {

    PENDING(0, "待开票"),
    UPLOADED(1, "已上传"),
    VERIFIED(2, "已验真"),
    CERTIFIED(3, "已认证"),
    VOIDED(4, "已作废");

    private final Integer code;
    private final String desc;

    InvoiceStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}