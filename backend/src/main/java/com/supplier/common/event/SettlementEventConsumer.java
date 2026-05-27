package com.supplier.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对账领域事件消费者
 * <p>
 * 消费 supplier.settlement.queue 队列中的对账事件：
 * settlement.reconciliation.sent / settlement.reconciliation.confirmed
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "supplier.settlement.queue")
    public void handleSettlementEvent(DomainEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.info("重复对账事件忽略: {}", event.getEventId());
            return;
        }

        log.info("收到对账事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        try {
            switch (event.getEventType()) {
                case "settlement.reconciliation.sent"     -> handleSent(event);
                case "settlement.reconciliation.confirmed" -> handleConfirmed(event);
                default -> log.warn("未知对账事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理对账事件失败: type={}, eventId={}", event.getEventType(), event.getEventId(), e);
            throw e;
        }
    }

    private void handleSent(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("对账单发送事件: reconciliationId={}", data.get("businessId"));
    }

    private void handleConfirmed(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("对账确认事件: reconciliationId={}", data.get("businessId"));
    }

    private boolean isDuplicate(String eventId) {
        String key = "idempotent:event:" + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(success);
    }
}