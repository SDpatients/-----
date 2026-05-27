package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum AppealStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    AUDITING(2, "审核中"),
    APPROVED(3, "通过"),
    REJECTED(4, "驳回");

    private final Integer code;
    private final String desc;

    AppealStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}