package com.supplier.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitDomainEventPublisher implements DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String exchange, String routingKey, DomainEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("领域事件已发布: {}, {}", event.getEventType(), event.getEventId());
    }
}
