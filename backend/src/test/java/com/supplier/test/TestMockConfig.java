package com.supplier.test;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 测试环境 Mock Bean 配置
 * <p>
 * 为测试环境提供 RabbitMQ / Redis 等外部中间件的完整 Mock 替代，
 * Mock 行为：Redis 缓存始终未命中（cache miss），RabbitMQ 静默丢弃消息。
 */
@TestConfiguration
public class TestMockConfig {

    /**
     * Mock RedisConnectionFactory —— Feed 进 mock RedisTemplate
     */
    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
        RedisConnection conn = Mockito.mock(RedisConnection.class);
        Mockito.when(factory.getConnection()).thenReturn(conn);
        return factory;
    }

    /**
     * Mock RedisTemplate —— opsForValue().get() 返回 null（缓存未命中），set() 无操作
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> mockRedisTemplate() {
        RedisTemplate<String, Object> template = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = Mockito.mock(ValueOperations.class);

        // get() → null（始终缓存未命中）
        Mockito.when(valueOps.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(template.opsForValue()).thenReturn(valueOps);

        // hasKey() → false（不命中黑名单）
        Mockito.when(template.hasKey(Mockito.anyString())).thenReturn(false);

        // delete() → true（模拟成功）
        Mockito.when(template.delete(Mockito.anyString())).thenReturn(true);

        return template;
    }

    /**
     * Mock RabbitMQ ConnectionFactory —— 返回可用的 mock Connection
     */
    @Bean
    @Primary
    public ConnectionFactory mockRabbitConnectionFactory() {
        ConnectionFactory factory = Mockito.mock(ConnectionFactory.class);
        Connection conn = Mockito.mock(Connection.class);
        Mockito.when(factory.createConnection()).thenReturn(conn);
        return factory;
    }

    /**
     * Mock RabbitTemplate —— 静默丢弃所有消息
     */
    @Bean
    @Primary
    public RabbitTemplate mockRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}