package com.supplier.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 质检领域事件消费者
 * <p>
 * 消费 supplier.quality.queue 队列中的质检事件：
 * quality.inspection.submitted
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QualityEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "supplier.quality.queue")
    public void handleQualityEvent(DomainEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.info("重复质检事件忽略: {}", event.getEventId());
            return;
        }

        log.info("收到质检事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        try {
            if ("quality.inspection.submitted".equals(event.getEventType())) {
                handleInspectionSubmitted(event);
            } else {
                log.warn("未知质检事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理质检事件失败: type={}, eventId={}", event.getEventType(), event.getEventId(), e);
            throw e;
        }
    }

    private void handleInspectionSubmitted(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("质检提交事件: inspectionId={}", data.get("businessId"));
    }

    private boolean isDuplicate(String eventId) {
        String key = "idempotent:event:" + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(success);
    }
}