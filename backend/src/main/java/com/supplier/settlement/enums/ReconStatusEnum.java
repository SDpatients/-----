package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum ReconStatusEnum {

    DRAFT(0, "草稿"),
    SENT(1, "已发送"),
    CONFIRMED(2, "已确认"),
    DISPUTED(3, "有争议"),
    CLOSED(4, "已关闭");

    private final Integer code;
    private final String desc;

    ReconStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}