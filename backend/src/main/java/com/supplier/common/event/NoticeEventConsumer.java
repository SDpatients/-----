package com.supplier.common.event;

import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通知事件消费者
 * <p>
 * 消费 supplier.notice.queue 队列中的通知事件，负责：
 * 站内信创建、待办创建、邮件/短信/企微等多渠道通知分发
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageNoticeService messageNoticeService;
    private final PortalTodoService portalTodoService;

    @RabbitListener(queues = "supplier.notice.queue")
    public void handleNoticeEvent(DomainEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.info("重复通知事件忽略: {}", event.getEventId());
            return;
        }

        log.info("收到通知事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        try {
            switch (event.getEventType()) {
                case "notice.message" -> handleMessage(event);
                case "notice.todo"    -> handleTodo(event);
                default -> log.warn("未知通知事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理通知事件失败: type={}, eventId={}", event.getEventType(), event.getEventId(), e);
            throw e;
        }
    }

    /**
     * 创建站内信通知
     */
    private void handleMessage(DomainEvent event) {
        Map<String, Object> data = event.getData();
        MessageNoticeCreateDTO dto = new MessageNoticeCreateDTO();
        dto.setReceiverUserId(toLong(data.get("receiverUserId")));
        dto.setReceiverSupplierId(toLong(data.get("receiverSupplierId")));
        dto.setChannel(toInt(data.get("channel"), 1));
        dto.setTitle((String) data.get("title"));
        dto.setContent((String) data.get("content"));
        dto.setBusinessType((String) data.get("businessType"));
        dto.setBusinessId(toLong(data.get("businessId")));
        Long id = messageNoticeService.create(dto);
        log.info("站内信已创建: id={}, title={}", id, dto.getTitle());
    }

    /**
     * 创建待办
     */
    private void handleTodo(DomainEvent event) {
        Map<String, Object> data = event.getData();
        PortalTodoCreateDTO dto = new PortalTodoCreateDTO();
        dto.setUserId(toLong(data.get("userId")));
        dto.setSupplierId(toLong(data.get("supplierId")));
        dto.setTodoType((String) data.get("todoType"));
        dto.setBusinessType((String) data.get("businessType"));
        dto.setBusinessId(toLong(data.get("businessId")));
        dto.setBusinessNo((String) data.get("businessNo"));
        dto.setTitle((String) data.get("title"));
        Long id = portalTodoService.create(dto);
        log.info("待办已创建: id={}, title={}", id, dto.getTitle());
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object value, Integer defaultVal) {
        if (value == null) return defaultVal;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private boolean isDuplicate(String eventId) {
        String key = "idempotent:event:" + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(success);
    }
}