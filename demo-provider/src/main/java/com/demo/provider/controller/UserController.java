package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import com.demo.provider.config.RedisUtil;
import com.demo.provider.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("demo/provider")
@RefreshScope
public class UserController {

    @Value("${provider.user.name}")
    private String name;

    @RequestMapping("/getUser")
    public CommonResponse<Object> getUser(){
        User user = new User();
        user.setName("里");
        user.setPhone(123124324);
        return CommonResponse.success(user, "success");
    }

    @RequestMapping("/getValue")
    public CommonResponse<Object> getValue(){
        return CommonResponse.success(name, "success");
    }

    @RequestMapping("/setCache")
    public CommonResponse<Object> setCache(){
        return CommonResponse.success(name, "success");
    }
}
