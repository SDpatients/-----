package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum DeductionStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    CONFIRMED(2, "已确认"),
    DISPUTED(3, "有异议"),
    BOOKED(4, "已入账");

    private final Integer code;
    private final String desc;

    DeductionStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}