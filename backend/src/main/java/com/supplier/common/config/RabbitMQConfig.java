package com.supplier.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── 交换机 ──
    public static final String ORDER_EXCHANGE = "supplier.order.exchange";
    public static final String DELIVERY_EXCHANGE = "supplier.delivery.exchange";
    public static final String QUALITY_EXCHANGE = "supplier.quality.exchange";
    public static final String SETTLEMENT_EXCHANGE = "supplier.settlement.exchange";
    public static final String NOTICE_EXCHANGE = "supplier.notice.exchange";
    public static final String INTEGRATION_EXCHANGE = "supplier.integration.exchange";
    public static final String DELAY_EXCHANGE = "supplier.delay.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "supplier.dlx.exchange";

    // ── 队列 ──
    public static final String ORDER_QUEUE = "supplier.order.queue";
    public static final String DELIVERY_QUEUE = "supplier.delivery.queue";
    public static final String QUALITY_QUEUE = "supplier.quality.queue";
    public static final String SETTLEMENT_QUEUE = "supplier.settlement.queue";
    public static final String NOTICE_QUEUE = "supplier.notice.queue";
    public static final String INTEGRATION_QUEUE = "supplier.integration.queue";
    public static final String DELAY_QUEUE = "supplier.delay.queue";
    public static final String DEAD_LETTER_QUEUE = "supplier.dlx.queue";

    // ── 路由键 ──
    public static final String ORDER_ROUTING_KEY = "supplier.order.routing";
    public static final String DELIVERY_ROUTING_KEY = "supplier.delivery.routing";
    public static final String QUALITY_ROUTING_KEY = "supplier.quality.routing";
    public static final String SETTLEMENT_ROUTING_KEY = "supplier.settlement.routing";
    public static final String NOTICE_ROUTING_KEY = "supplier.notice.routing";
    public static final String INTEGRATION_ROUTING_KEY = "supplier.integration.routing";
    public static final String DELAY_ROUTING_KEY = "supplier.delay.routing";
    public static final String DEAD_LETTER_ROUTING_KEY = "supplier.dlx.routing";

    // ── 通用 Bean ──

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // ── 订单交换机/队列 ──

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    // ── 发货交换机/队列 ──

    @Bean
    public DirectExchange deliveryExchange() {
        return new DirectExchange(DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public Queue deliveryQueue() {
        return QueueBuilder.durable(DELIVERY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue())
                .to(deliveryExchange())
                .with(DELIVERY_ROUTING_KEY);
    }

    // ── 质检交换机/队列 ──

    @Bean
    public DirectExchange qualityExchange() {
        return new DirectExchange(QUALITY_EXCHANGE, true, false);
    }

    @Bean
    public Queue qualityQueue() {
        return QueueBuilder.durable(QUALITY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding qualityBinding() {
        return BindingBuilder.bind(qualityQueue())
                .to(qualityExchange())
                .with(QUALITY_ROUTING_KEY);
    }

    // ── 对账交换机/队列 ──

    @Bean
    public DirectExchange settlementExchange() {
        return new DirectExchange(SETTLEMENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue settlementQueue() {
        return QueueBuilder.durable(SETTLEMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding settlementBinding() {
        return BindingBuilder.bind(settlementQueue())
                .to(settlementExchange())
                .with(SETTLEMENT_ROUTING_KEY);
    }

    // ── 通知交换机/队列 ──

    @Bean
    public DirectExchange noticeExchange() {
        return new DirectExchange(NOTICE_EXCHANGE, true, false);
    }

    @Bean
    public Queue noticeQueue() {
        return QueueBuilder.durable(NOTICE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding noticeBinding() {
        return BindingBuilder.bind(noticeQueue())
                .to(noticeExchange())
                .with(NOTICE_ROUTING_KEY);
    }

    // ── 集成网关交换机/队列 ──

    @Bean
    public DirectExchange integrationExchange() {
        return new DirectExchange(INTEGRATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue integrationQueue() {
        return QueueBuilder.durable(INTEGRATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding integrationBinding() {
        return BindingBuilder.bind(integrationQueue())
                .to(integrationExchange())
                .with(INTEGRATION_ROUTING_KEY);
    }

    // ── 延迟交换机/队列（用于延迟消费场景） ──

    @Bean
    public DirectExchange delayExchange() {
        return ExchangeBuilder.directExchange(DELAY_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(DELAY_ROUTING_KEY);
    }

    // ── 死信交换机/队列 ──

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }
}
