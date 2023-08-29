package com.kyrie.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MQCommonCinfig implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取rabbitTemplate对象
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        //配置ReturnsCallback
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.info("消息发送失败,应答码{},原因{},交换机{},路由键{},消息{}",
                    returnedMessage.getReplyCode(), //错误码
                    returnedMessage.getReplyText(), //错误信息
                    returnedMessage.getExchange(),  //交换机
                    returnedMessage.getRoutingKey(), //路由key
                    returnedMessage.getMessage().toString()); //消息对象

            // TODO 如果有需要，这里可以做消息的重发
        });

        //定义回调，消息到交换机的回调
        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    if (ack) {
                        //接收成功，消息到达路由器
                    } else {
                        //接收失败，消息未到达路由器
                    }
                }
        );

        //设置交换机处理失败消息的模式
        //消息处理失败会把消息返回给生产者
        rabbitTemplate.setMandatory(true); //这个好像在yml配置里面可以配置
    }

    /**
     * 定义失败重试次数上限的消息，重新发送到指定的交换机
     * @param rabbitTemplate
     * @return
     */
    @Bean
    public MessageRecoverer republishMessageRecover(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }
}
