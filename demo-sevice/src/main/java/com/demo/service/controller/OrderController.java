package com.demo.service.controller;

import com.demo.common.util.CommonResponse;
import com.demo.service.feign.UserFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo/order")
public class OrderController {

    @Autowired
    private UserFeignService feignService;

    @RequestMapping("/feign")
    public CommonResponse<Object> feign(){
        return CommonResponse.success(feignService.getUser(), "success");
    }
}
