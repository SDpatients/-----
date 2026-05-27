package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum DeductionTypeEnum {

    QUALITY(1, "质量扣款"),
    DELAY(2, "延期扣款"),
    SHORTAGE(3, "短交扣款"),
    OTHER(4, "其他扣款");

    private final Integer code;
    private final String desc;

    DeductionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}