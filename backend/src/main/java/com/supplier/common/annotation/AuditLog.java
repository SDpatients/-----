package com.supplier.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    /** 模块名称，如 "采购订单" */
    String module();
    /** 业务类型，如 "purchase_order" */
    String businessType();
    /** 动作名称，如 "订单下发" */
    String action();
    /** SpEL 表达式获取业务 ID，如 "#id" */
    String businessIdExpr() default "";
    /** SpEL 表达式获取业务单号，如 "#dto.orderNo" */
    String businessNoExpr() default "";
    /** 操作前状态，支持整数字面量(如 "0")或参数引用(如 "#status") */
    String beforeStatusExpr() default "";
    /** 操作后状态，支持整数字面量(如 "1")或参数引用(如 "#status") */
    String afterStatusExpr() default "";
}