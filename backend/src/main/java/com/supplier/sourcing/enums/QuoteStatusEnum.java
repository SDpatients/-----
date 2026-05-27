package com.supplier.sourcing.enums;

import lombok.Getter;

@Getter
public enum QuoteStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    ACCEPTED(2, "已采纳"),
    REJECTED(3, "未采纳"),
    WITHDRAWN(4, "已撤回"),
    PRICED(5, "已定价");

    private final Integer code;
    private final String desc;

    QuoteStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}