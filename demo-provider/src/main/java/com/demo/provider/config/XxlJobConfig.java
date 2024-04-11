package com.demo.provider.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    @Value("${xxl.job.accessToken}")
    private String accessToken;
    @Value("${xxl.job.executor.address}")
    private String executorAddress;

    @Value("${xxl.job.executor.appname}")
    private String executorAppname;

    @Value("${xxl.job.executor.ip}")
    private String executorIp;

    @Value("${xxl.job.executor.port}")
    private Integer executorPort;
    @Value("${xxl.job.executor.logpath}")
    private String executorLogpath;

    @Value("${xxl.job.executor.logretentiondays}")
    private Integer executorLogretentiondays;


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(executorAppname);
        xxlJobSpringExecutor.setIp(executorIp);
        xxlJobSpringExecutor.setPort(executorPort);
        xxlJobSpringExecutor.setLogPath(executorLogpath);
        xxlJobSpringExecutor.setLogRetentionDays(executorLogretentiondays);
        return xxlJobSpringExecutor;
    }


}
