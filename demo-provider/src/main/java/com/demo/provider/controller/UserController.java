package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import com.demo.provider.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo/provider")
@RefreshScope
public class UserController {

    @Value("${provider.user.name}")
    private String name;

    @RequestMapping("/getUser")
    public CommonResponse<Object> getUser(){
        User user = new User();
        user.setName("lili");
        user.setPhone(123124324);
        return CommonResponse.success(user, "success");
    }

    @RequestMapping("/getValue")
    public CommonResponse<Object> getValue(){
        return CommonResponse.success(name, "success");
    }
}
