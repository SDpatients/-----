package com.supplier.common.event;

public interface DomainEventPublisher {

    void publish(String exchange, String routingKey, DomainEvent event);
}
