package com.supplier.common.aspect;

import com.supplier.common.annotation.Idempotent;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String IDEMPOTENT_PREFIX = "idempotent:";
    private static final String HEADER_IDEMPOTENT_TOKEN = "X-Idempotent-Token";

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取请求上下文，跳过幂等校验");
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(HEADER_IDEMPOTENT_TOKEN);
        if (!StringUtils.hasText(token)) {
            throw BusinessException.of(ResultCode.PARAM_ERROR.getCode(), "缺少幂等令牌，请刷新页面后重试");
        }

        String key = IDEMPOTENT_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.FALSE.equals(deleted)) {
            log.warn("重复提交或令牌已过期: key={}", key);
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT);
        }

        log.debug("幂等校验通过: key={}", key);
        return joinPoint.proceed();
    }
}