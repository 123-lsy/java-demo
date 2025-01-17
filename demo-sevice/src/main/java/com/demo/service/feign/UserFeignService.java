package com.demo.service.feign;


import com.demo.common.util.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("demo-provider")
public interface UserFeignService {

    @RequestMapping("/demo/provider/getUser")
    CommonResponse<Object> getUser(@RequestParam("rpc") String rpc);
}
