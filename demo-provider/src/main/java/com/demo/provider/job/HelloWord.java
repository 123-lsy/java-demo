package com.demo.provider.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;
@Component
public class HelloWord {
    @XxlJob("helloWorld")
    public ReturnT<String> helloWorld(String param) throws Exception {
        XxlJobLogger.log("XXLJOB-HelloWord start...");
        System.out.println("Hello Word!");
        return ReturnT.SUCCESS;
    }
}
