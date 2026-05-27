package com.supplier.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
    @Builder.Default
    private String source = "supplier-collaboration";
    private String traceId;
    private Map<String, Object> data;
}
