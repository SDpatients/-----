package com.supplier.sourcing.enums;

import lombok.Getter;

@Getter
public enum QualificationStatusEnum {

    INVALID(0, "无效"),
    VALID(1, "有效"),
    EXPIRING(2, "即将过期"),
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String desc;

    QualificationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}