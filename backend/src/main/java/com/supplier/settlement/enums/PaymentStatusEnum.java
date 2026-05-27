package com.supplier.settlement.enums;

import lombok.Getter;

@Getter
public enum PaymentStatusEnum {

    PENDING(0, "待付款"),
    APPROVING(1, "审批中"),
    APPROVED(2, "已审批"),
    SCHEDULED(3, "已排期"),
    PAID(4, "已付款"),
    REJECTED(5, "已拒绝"),
    CANCELLED(6, "已取消");

    private final Integer code;
    private final String desc;

    PaymentStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}