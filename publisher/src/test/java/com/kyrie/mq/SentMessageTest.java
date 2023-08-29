package com.kyrie.mq;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest
public class SentMessageTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sentMessage() {
        //1 .准备消息
        //1.1 消息
        String msg = "测试发送消息：hello spring amqp!";
        //1.2获取持久化消息
        Message message = MessageBuilder
                .withBody("测试生成持久化消息".getBytes(StandardCharsets.UTF_8))    //消息体，要转成字节形式
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)   //持久化
                .build();


        //2. 发送消息
        //2.1 准备CorrelationData，消息id
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //2.2 准备confirmCallback
        correlationData.getFuture().addCallback(
                result -> {
                    // 判断结果
                    if (result.isAck()) {
                        // ACK
                        log.debug("消息成功投递到交换机！消息ID: {}", correlationData.getId());
                    } else {
                        // NACK
                        log.error("消息投递到交换机失败！消息ID：{}", correlationData.getId());
                        // 重发消息
                    }
                },
                ex -> {
                    // 记录日志
                    log.error("消息发送失败！", ex);
                    // 重发消息
                });
        //2.3 发送消息
        rabbitTemplate.convertAndSend("topic_exchange", "order.de", msg, correlationData);
    }

}
