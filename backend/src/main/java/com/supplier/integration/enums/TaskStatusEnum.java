package com.supplier.integration.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    IGNORED(4, "忽略");

    private final Integer code;
    private final String desc;

    TaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}