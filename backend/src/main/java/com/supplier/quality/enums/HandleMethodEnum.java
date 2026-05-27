package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum HandleMethodEnum {

    RETURN(1, "退货", "生成退货单，关联物流"),
    CONCESSION(2, "让步接收", "记录让步原因和审批"),
    SORTING(3, "挑选使用", "记录挑选结果"),
    SCRAP(4, "报废", "记录报废数量和原因"),
    DEDUCTION(5, "扣款", "生成扣款单");

    private final Integer code;
    private final String desc;
    private final String remark;

    HandleMethodEnum(Integer code, String desc, String remark) {
        this.code = code;
        this.desc = desc;
        this.remark = remark;
    }

    public static String getDesc(Integer code) {
        if (code == null) return "未知";
        for (HandleMethodEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return "未知(" + code + ")";
    }
}