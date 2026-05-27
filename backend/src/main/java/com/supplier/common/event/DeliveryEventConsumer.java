package com.supplier.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 发货领域事件消费者
 * <p>
 * 消费 supplier.delivery.queue 队列中的发货事件：
 * delivery.asn.created / delivery.asn.shipped / delivery.receipt.confirmed
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "supplier.delivery.queue")
    public void handleDeliveryEvent(DomainEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.info("重复发货事件忽略: {}", event.getEventId());
            return;
        }

        log.info("收到发货事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        try {
            switch (event.getEventType()) {
                case "delivery.asn.created"      -> handleAsnCreated(event);
                case "delivery.asn.shipped"      -> handleAsnShipped(event);
                case "delivery.receipt.confirmed" -> handleReceiptConfirmed(event);
                default -> log.warn("未知发货事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理发货事件失败: type={}, eventId={}", event.getEventType(), event.getEventId(), e);
            throw e;
        }
    }

    private void handleAsnCreated(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("ASN 创建事件: noticeId={}", data.get("businessId"));
    }

    private void handleAsnShipped(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("ASN 发货事件: noticeId={}", data.get("businessId"));
    }

    private void handleReceiptConfirmed(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("收货确认事件: receiptId={}", data.get("businessId"));
    }

    private boolean isDuplicate(String eventId) {
        String key = "idempotent:event:" + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(success);
    }
}