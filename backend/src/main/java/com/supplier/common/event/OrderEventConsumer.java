package com.supplier.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 订单领域事件消费者
 * <p>
 * 消费 supplier.order.queue 队列中的订单事件：
 * order.purchase.published / order.purchase.confirmed / order.purchase.rejected / order.purchase.canceled
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "supplier.order.queue")
    public void handleOrderEvent(DomainEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.info("重复订单事件忽略: {}", event.getEventId());
            return;
        }

        log.info("收到订单事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        try {
            switch (event.getEventType()) {
                case "order.purchase.published" -> handlePublished(event);
                case "order.purchase.confirmed"  -> handleConfirmed(event);
                case "order.purchase.rejected"   -> handleRejected(event);
                case "order.purchase.canceled"   -> handleCanceled(event);
                default -> log.warn("未知订单事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理订单事件失败: type={}, eventId={}", event.getEventType(), event.getEventId(), e);
            throw e; // 抛出异常触发重试/进入死信
        }
    }

    private void handlePublished(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("订单下发事件: orderId={}, supplierId={}", data.get("businessId"), data.get("supplierId"));
        // 由 NoticeEventConsumer 统一处理通知和待办创建
    }

    private void handleConfirmed(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("订单确认事件: orderId={}, supplierId={}", data.get("businessId"), data.get("supplierId"));
    }

    private void handleRejected(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("订单拒单事件: orderId={}, supplierId={}", data.get("businessId"), data.get("supplierId"));
    }

    private void handleCanceled(DomainEvent event) {
        Map<String, Object> data = event.getData();
        log.info("订单取消事件: orderId={}", data.get("businessId"));
    }

    /**
     * 幂等检查：以 eventId 为 key 在 Redis 中记录，5 分钟内重复则跳过
     */
    private boolean isDuplicate(String eventId) {
        String key = "idempotent:event:" + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(success);
    }
}