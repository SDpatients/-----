package com.supplier.common.aspect;

import com.supplier.common.annotation.AuditLog;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.system.entity.SysAuditLog;
import com.supplier.system.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final String TRACE_ID_KEY = "traceId";

    private final SysAuditLogMapper sysAuditLogMapper;

    @AfterReturning("@annotation(auditLog)")
    public void writeLog(JoinPoint joinPoint, AuditLog auditLog) {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            SysAuditLog audit = new SysAuditLog();
            audit.setTraceId(getTraceId());
            audit.setUserId(loginUser != null ? loginUser.getUserId() : null);
            audit.setUsername(loginUser != null ? loginUser.getRealName() : null);
            audit.setModuleName(auditLog.module());
            audit.setBusinessType(auditLog.businessType());
            audit.setActionName(auditLog.action());
            audit.setBusinessId(resolveExpr(auditLog.businessIdExpr(), joinPoint));
            audit.setBusinessNo(resolveStrExpr(auditLog.businessNoExpr(), joinPoint));
            audit.setRequestMethod(getRequestMethod(joinPoint));
            audit.setRequestPath(getRequestPath());
            audit.setClientIp(getClientIp());
            audit.setResultStatus(1);
            audit.setOperateTime(LocalDateTime.now());

            sysAuditLogMapper.insert(audit);
        } catch (Exception e) {
            log.error("写入审计日志失败: module={}, action={}", auditLog.module(), auditLog.action(), e);
        }
    }

    private Long resolveExpr(String expr, JoinPoint joinPoint) {
        if (!StringUtils.hasText(expr)) {
            return null;
        }
        try {
            // 简单解析：从方法参数名匹配
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            // 去除 SpEL 的 # 前缀
            String paramName = expr.startsWith("#") ? expr.substring(1) : expr;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramName.equals(paramNames[i]) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: expr={}", expr, e);
        }
        return null;
    }

    private String resolveStrExpr(String expr, JoinPoint joinPoint) {
        if (!StringUtils.hasText(expr)) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            String paramName = expr.startsWith("#") ? expr.substring(1) : expr;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramName.equals(paramNames[i]) && args[i] instanceof String) {
                    return (String) args[i];
                }
            }
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: expr={}", expr, e);
        }
        return null;
    }

    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return StringUtils.hasText(traceId) ? traceId : null;
    }

    private String getRequestMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getName();
    }

    private String getRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}