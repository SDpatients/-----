package com.supplier.logistics.enums;

import lombok.Getter;

@Getter
public enum DemandStatusEnum {

    PENDING(0, "待处理"),
    PUBLISHED(1, "已发布"),
    RESPONDED(2, "已响应"),
    CLOSED(3, "已关闭");

    private final Integer code;
    private final String desc;

    DemandStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}