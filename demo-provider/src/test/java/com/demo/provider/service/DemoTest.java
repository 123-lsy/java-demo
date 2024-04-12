package com.demo.provider.service;


import com.demo.provider.config.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;


@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DemoTest {

    @Autowired
    RedisUtil redisUtil;

    @Test
    public void redisTest(){
        String key = "demo:prvoider:cache";
        redisUtil.set(key, "缓存");
        System.out.println(redisUtil.get(key));

        String spuKey = "demo:prvoider:spu";
        long spu = redisUtil.incrementAndGet(spuKey);
        log.info("Redis 自增主键使用:{}", spu);


        String lock = "demo:prvoider:lock";
        redisUtil.setIfAbsent(lock, 1, 10L, null);
        redisUtil.setIfAbsent(lock,2 , 10L, TimeUnit.MILLISECONDS);


    }

}
