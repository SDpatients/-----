package com.supplier.common.aspect;

import com.supplier.common.annotation.AuditLog;
import com.supplier.common.config.RabbitMQConfig;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.system.entity.SysAuditLog;
import com.supplier.system.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final String TRACE_ID_KEY = "traceId";

    private final SysAuditLogMapper sysAuditLogMapper;
    private final DomainEventPublisher domainEventPublisher;

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void writeLog(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        try {
            SysAuditLog audit = buildAuditLog(joinPoint, auditLog, result);
            audit.setResultStatus(1);
            audit.setOperateTime(LocalDateTime.now());
            sysAuditLogMapper.insert(audit);
        } catch (Exception e) {
            log.error("写入审计日志失败: module={}, action={}", auditLog.module(), auditLog.action(), e);
        }
    }

    @AfterThrowing(pointcut = "@annotation(auditLog)", throwing = "ex")
    public void writeErrorLog(JoinPoint joinPoint, AuditLog auditLog, Exception ex) {
        SysAuditLog audit = null;
        try {
            audit = buildAuditLog(joinPoint, auditLog, null);
            audit.setResultStatus(0);
            audit.setErrorMessage(truncate(ex.getMessage(), 500));
            audit.setOperateTime(LocalDateTime.now());
            sysAuditLogMapper.insert(audit);
        } catch (Exception e) {
            log.error("写入异常审计日志失败: module={}, action={}", auditLog.module(), auditLog.action(), e);
        }
        // 写完日志后立即发布异常告警事件，由 NoticeEventConsumer 转为 portal_todo 推送给平台管理员
        publishFailureAlert(auditLog, audit, ex);
    }

    /**
     * 将审计失败事件发布到通知交换机，下游 {@link com.supplier.common.event.NoticeEventConsumer}
     * 会消费并创建 portal_todo。MQ 异常不会反向影响 AOP 主流程。
     */
    private void publishFailureAlert(AuditLog auditLog, SysAuditLog audit, Exception ex) {
        try {
            Map<String, Object> data = new HashMap<>();
            // 收件人：默认平台超级管理员 userId=1，可由后续接入"系统配置"覆盖
            data.put("userId", 1L);
            data.put("todoType", "audit_alert");
            data.put("businessType", auditLog.module());
            if (audit != null && audit.getId() != null) {
                data.put("businessId", audit.getId());
            }
            if (audit != null) {
                data.put("businessNo", audit.getBusinessNo());
            }
            String title = String.format("[异常告警] %s-%s 失败", auditLog.module(), auditLog.action());
            String content = String.format("用户 %s 在 %s 执行【%s】失败：%s",
                    audit != null && audit.getUsername() != null ? audit.getUsername() : "匿名",
                    audit != null && audit.getOperateTime() != null ? audit.getOperateTime() : LocalDateTime.now(),
                    auditLog.action(),
                    ex.getMessage());
            data.put("title", title);
            data.put("content", content);

            DomainEvent event = DomainEvent.builder()
                    .eventType("notice.todo")
                    .traceId(audit != null ? audit.getTraceId() : MDC.get(TRACE_ID_KEY))
                    .data(data)
                    .build();
            domainEventPublisher.publish(RabbitMQConfig.NOTICE_EXCHANGE, RabbitMQConfig.NOTICE_ROUTING_KEY, event);
            log.warn("审计异常告警已发布: module={}, action={}, traceId={}",
                    auditLog.module(), auditLog.action(), event.getTraceId());
        } catch (Exception e) {
            // MQ 故障不能影响 AOP 主链路
            log.error("发布审计异常告警失败: module={}, action={}", auditLog.module(), auditLog.action(), e);
        }
    }

    private SysAuditLog buildAuditLog(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysAuditLog audit = new SysAuditLog();
        audit.setTraceId(getTraceId());
        audit.setUserId(loginUser != null ? loginUser.getUserId() : null);
        audit.setUsername(loginUser != null ? loginUser.getRealName() : null);
        audit.setModuleName(auditLog.module());
        audit.setBusinessType(auditLog.businessType());
        audit.setActionName(auditLog.action());
        audit.setBusinessId(resolveExpr(auditLog.businessIdExpr(), joinPoint, result));
        audit.setBusinessNo(resolveStrExpr(auditLog.businessNoExpr(), joinPoint, result));
        audit.setBeforeStatus(resolveStatusExpr(auditLog.beforeStatusExpr(), joinPoint, result));
        audit.setAfterStatus(resolveStatusExpr(auditLog.afterStatusExpr(), joinPoint, result));
        audit.setRequestMethod(getRequestMethod(joinPoint));
        audit.setRequestPath(getRequestPath());
        audit.setClientIp(getClientIp());
        return audit;
    }

    private Long resolveExpr(String expr, JoinPoint joinPoint, Object result) {
        if (!StringUtils.hasText(expr)) {
            return null;
        }
        // 支持 #result 获取方法返回值
        if ("#result".equals(expr)) {
            if (result instanceof Long) {
                return (Long) result;
            }
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

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

    private String resolveStrExpr(String expr, JoinPoint joinPoint, Object result) {
        if (!StringUtils.hasText(expr)) {
            return null;
        }
        // 支持 #result 获取方法返回值
        if ("#result".equals(expr)) {
            if (result instanceof String) {
                return (String) result;
            }
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

    /**
     * 解析状态表达式，支持整数字面量(如 "0", "1")和参数引用(如 "#status")
     */
    private Integer resolveStatusExpr(String expr, JoinPoint joinPoint, Object result) {
        if (!StringUtils.hasText(expr)) {
            return null;
        }
        // 先尝试整数字面量
        try {
            return Integer.parseInt(expr.trim());
        } catch (NumberFormatException ignored) {
        }
        // 再尝试参数引用
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            String paramName = expr.startsWith("#") ? expr.substring(1) : expr;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramName.equals(paramNames[i])) {
                    if (args[i] instanceof Integer) {
                        return (Integer) args[i];
                    }
                    if (args[i] instanceof Long) {
                        return ((Long) args[i]).intValue();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析状态表达式失败: expr={}", expr, e);
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

    private String truncate(String str, int maxLen) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
