package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum NcrStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PROCESSING(2, "处理中"),
    PENDING_VERIFY(3, "待验证"),
    CLOSED(4, "已关闭"),
    CANCELED(5, "已取消");

    private final Integer code;
    private final String desc;

    NcrStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}