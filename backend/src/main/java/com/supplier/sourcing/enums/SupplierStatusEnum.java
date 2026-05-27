package com.supplier.sourcing.enums;

import lombok.Getter;

/**
 * 供应商状态枚举
 * 0=待审核 1=初审中 2=复审中 3=终审中 4=审核通过 5=审核驳回 6=已禁用
 */
@Getter
public enum SupplierStatusEnum {

    PENDING(0, "待审核"),
    FIRST_AUDITING(1, "初审中"),
    SECOND_AUDITING(2, "复审中"),
    FINAL_AUDITING(3, "终审中"),
    APPROVED(4, "审核通过"),
    REJECTED(5, "审核驳回"),
    DISABLED(6, "已禁用");

    private final Integer code;
    private final String desc;

    SupplierStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SupplierStatusEnum fromCode(Integer code) {
        for (SupplierStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}