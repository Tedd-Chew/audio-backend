package com.example.audiobackend.rabbitmq.consumer;

import com.example.audiobackend.constant.MqConstant;
import com.example.audiobackend.entity.AiQuestionMessage;
import com.example.audiobackend.service.ChatService; // 你的AI业务服务

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AiQuestionConsumer {

    // 1. 注入AI业务服务（纯逻辑，和MQ解耦）
    @Autowired
    private ChatService aiChatService;

    /**
     * 核心：监听MQ队列，自动接收消息
     */
    @RabbitListener(queues = MqConstant.AI_QUESTION_QUEUE)//执行到这里领走消息
    @RabbitHandler
    public void receive(AiQuestionMessage message) {
        // 从消息对象中获取参数
        String visitorId = message.getVisitorId();
        String question = message.getQuestion();
        try {
        aiChatService.chat(visitorId, question);
        } catch (Exception e) {
            // 异常处理：失败也存Redis，告诉前端
            System.err.println("【消费者】AI处理异常：" + e.getMessage());
        }
    }
}

