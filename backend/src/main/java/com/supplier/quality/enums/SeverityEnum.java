package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum SeverityEnum {

    GENERAL(1, "一般"),
    SERIOUS(2, "严重"),
    MAJOR(3, "重大");

    private final Integer code;
    private final String desc;

    SeverityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}