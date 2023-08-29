package com.kyrie.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BindingMQ {

    /**
     * 定义持久化通配符交换机
     * @return
     */
    @Bean("beanNameExchange")
    public Exchange amqpExchange(){
        return ExchangeBuilder.topicExchange("topic_exchange").durable(true).build();
    }

    /**
     * 定义持久化队列
     * @return
     */
    @Bean("beanNameQueue")
    public Queue amqpQueue(){
        return QueueBuilder.durable("topic_queue").build();
    }

    /**
     * 绑定队列到交换机，并设置路由key
     * @param queue 根据Bean的名字传入
     * @param exchange  根据Bean的名字传入
     * @return
     */
    @Bean
    public Binding bindingQueueExchange(@Qualifier("beanNameQueue") Queue queue, @Qualifier("beanNameExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("order.#").noargs();
    }
}
