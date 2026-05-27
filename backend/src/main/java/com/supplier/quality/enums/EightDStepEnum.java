package com.supplier.quality.enums;

import lombok.Getter;

@Getter
public enum EightDStepEnum {

    D1(1, "D1-组建团队", 3),
    D2(2, "D2-问题描述", 3),
    D3(3, "D3-遏制措施", 3),
    D4(4, "D4-根因分析", 5),
    D5(5, "D5-纠正措施", 5),
    D6(6, "D6-验证效果", 5),
    D7(7, "D7-预防措施", 5),
    D8(8, "D8-结案", 3);

    private final Integer code;
    private final String desc;
    private final Integer defaultDays;

    EightDStepEnum(Integer code, String desc, Integer defaultDays) {
        this.code = code;
        this.desc = desc;
        this.defaultDays = defaultDays;
    }

    public static String getDesc(Integer code) {
        if (code == null) return "未知";
        for (EightDStepEnum e : values()) {
            if (e.code.equals(code)) return e.desc;
        }
        return "未知(" + code + ")";
    }

    public static EightDStepEnum next(Integer code) {
        if (code == null || code >= 8) return null;
        for (EightDStepEnum e : values()) {
            if (e.code == code + 1) return e;
        }
        return null;
    }
}