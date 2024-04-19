package com.demo.provider.controller;

import ch.qos.logback.classic.sift.SiftAction;
import com.demo.common.util.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/kafka")
public class KafkaProduceController {
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Transactional
    @GetMapping("/send")
    public CommonResponse<Object> sender(){
        String msg = "hello world!";
        kafkaTemplate.send("test", msg);
        return CommonResponse.success(null, "success");
    }
    @Transactional
    @GetMapping("/send/callback")
    public CommonResponse<Object> callback(){
        String msg = "hello world!";
        kafkaTemplate.send("test", msg).addCallback(new SuccessCallback<SendResult<Object, Object>>() {
            @Override
            public void onSuccess(SendResult<Object, Object> objectObjectSendResult) {
                log.info("消息发送成功");
            }
        }, new FailureCallback() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("消息发送失败:{}, topic:{}, msg:{}", throwable.getCause(), "test", msg);
                //补偿机制
            }
        });
        return CommonResponse.success(null, "success");

    }

    @GetMapping("/send/transaction")
    public CommonResponse<Object> transaction(){
        kafkaTemplate.executeInTransaction(operations -> {
          operations.send("test", "kafka事务消息发送");
          int i  = 1/0;
          operations.send("test", "kafka事务消息发送2");
          return true;
        });
        return CommonResponse.success(null, "success");
    }
}
