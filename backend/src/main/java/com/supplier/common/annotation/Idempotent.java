package com.supplier.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /** 幂等键前缀，如 'order_create' */
    String keyPrefix() default "";
    /** 幂等键过期时间（秒），默认 5 分钟 */
    long expireSeconds() default 300;
}