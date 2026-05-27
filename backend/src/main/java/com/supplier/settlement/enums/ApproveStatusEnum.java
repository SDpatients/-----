package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum ApproveStatusEnum {

    NOT_SUBMITTED(0, "未提交审批"),
    PENDING(1, "审批中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回");

    private final Integer code;
    private final String desc;

    ApproveStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}