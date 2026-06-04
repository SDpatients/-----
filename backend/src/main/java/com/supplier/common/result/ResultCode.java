package com.supplier.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(40001, "参数错误"),
    NOT_FOUND(40002, "资源不存在"),
    UNAUTHORIZED(40101, "未登录"),
    TOKEN_EXPIRED(40102, "令牌过期"),
    FORBIDDEN(40301, "无权限"),
    DATA_FORBIDDEN(40302, "数据越权"),
    STATUS_NOT_ALLOWED(40901, "状态不允许"),
    DUPLICATE_SUBMIT(40902, "重复提交"),
    INTERNAL_ERROR(50001, "系统异常"),
    EXTERNAL_ERROR(50002, "外部系统异常"),
    REDIS_ERROR(50003, "缓存服务异常"),
    BUSINESS_ERROR(40900, "业务异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
