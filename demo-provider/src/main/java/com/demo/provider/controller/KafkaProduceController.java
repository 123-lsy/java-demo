package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaProduceController {
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @GetMapping("/send")
    public CommonResponse<Object> sender(){
        String msg = "hello world!";
        kafkaTemplate.send("test", msg);
        return CommonResponse.success(null, "success");
    }
}
