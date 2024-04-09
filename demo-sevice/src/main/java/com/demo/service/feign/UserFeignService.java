package com.demo.service.feign;


import com.demo.common.util.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("demo-provider")
public interface UserFeignService {

    @RequestMapping("demo/provider/getUser")
    CommonResponse<Object> getUser();
}
