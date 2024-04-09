package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import com.demo.provider.dto.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo/provider")
public class UserController {

    @RequestMapping("/getUser")
    public CommonResponse<Object> getUser(){
        User user = new User();
        user.setName("lili");
        user.setPhone(123124324);
        return CommonResponse.success(user, "success");
    }
}
