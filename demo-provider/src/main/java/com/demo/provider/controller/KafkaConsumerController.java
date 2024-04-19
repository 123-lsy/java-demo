package com.demo.provider.controller;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Consumer;

@Component
public class KafkaConsumerController {

    @KafkaListener(topics = {"test"})
    public void listen1(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            //用于测试异常处理
            //int i = 1 / 0;
            System.out.println("简单消费：" + record.topic() + "-" + record.partition() + "=" +
                    record.value());
        } catch (Exception e) {
            System.out.println("消费失败：" + e);
        }finally {
            //手动确认
            ack.acknowledge();
        }
    }

}
