package com.supplier.sourcing.enums;

import lombok.Getter;

@Getter
public enum RfqStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    QUOTING(2, "报价中"),
    CLOSED(3, "已截止"),
    PRICED(4, "已定价"),
    CANCELED(5, "已取消");

    private final Integer code;
    private final String desc;

    RfqStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}