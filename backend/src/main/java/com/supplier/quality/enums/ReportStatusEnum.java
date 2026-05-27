package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    AUDITING(2, "审核中"),
    REJECTED(3, "退回"),
    CLOSED(4, "已关闭");

    private final Integer code;
    private final String desc;

    ReportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}