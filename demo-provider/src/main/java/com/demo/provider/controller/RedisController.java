package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import com.demo.provider.config.RedisUtil;
import com.demo.provider.dto.User;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@RestController
@RequestMapping("/demo/redis")
public class RedisController {



    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String key = "demo:provider:user:";
    private static final String key1 = "demo:provider:lock";

    @RequestMapping("/setByTemplate")
    public CommonResponse<Object> setByTemplate(){
        User user = new User();
        user.setId(4);
        user.setName("李四");
        user.setPhone(123124324);
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(key+user.getId(), user, 60L, TimeUnit.SECONDS);
        return CommonResponse.success(user, "success");
    }

    @RequestMapping("/setByUtil")
    public CommonResponse<Object> setByUtil(){
        User user = new User();
        user.setId(5);
        user.setName("王五");
        user.setPhone(123124324);
        redisUtil.set(key+user.getId(), user, 60L, TimeUnit.SECONDS);
        return CommonResponse.success(user, "success");
    }
    @RequestMapping("/getByUtil")
    public CommonResponse<Object> getByUtil(Integer id){
        return CommonResponse.success(redisUtil.get(key + id), "success");
    }

    @RequestMapping("/setIncrement")
    public CommonResponse<Object> setIncrement(){
        String key = "demo:provider:seq";
        long l = redisUtil.incrementAndGet(key);
        return CommonResponse.success(l, "success");
    }

    @RequestMapping("/lock")
    public CommonResponse<Object> lock(){
        Boolean acquire = redisUtil.setIfAbsent(key1, System.currentTimeMillis(), 30L, TimeUnit.SECONDS);
        if (acquire){
            try {
                sleep(50000L);
                System.out.println();
            } catch (Exception e) {
                redisUtil.remove(key1);
            }
        }
        return CommonResponse.success(null, "success");
    }
}
