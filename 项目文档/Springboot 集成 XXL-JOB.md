# SpringBoot 集成 XXL-JOB

> - 1、**简单**：支持通过Web页面对任务进行CRUD操作，操作简单，一分钟上手；
> - 2、动态：支持**动态**修改任务状态、启动/停止任务，以及终止运行中任务，即时生效；
> - 3、调度中心HA（中心式）：调度采用中心式设计，“调度中心”自研调度组件并支持集群部署，可保证调度中心HA；
> - 4、执行器HA（分布式）：任务分布式执行，任务"执行器"支持**集群部署**，可保证任务执行HA；

搭建服务端和更详细的内容可见 : [官方中文文档](https://www.xuxueli.com/xxl-job)

xxl-job的服务端的核心部分主要是任务管理和执行器管理 , 创建好执行器后,  再创建任务, 任务是由执行器来执行的 ,架构图如下:

![image-20240410181433904](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240410181433904.png)

## 1.依赖

```java
  <dependency>
            <groupId>com.xuxueli</groupId>
            <artifactId>xxl-job-core</artifactId>
            <version>2.2.0</version>
        </dependency>
```

## 2.执行器部署

### 步骤一:nacos配置

放在nacos中的配置的value值不能为空, 否则读取key时会报空指针异常, 默认值可以为空字符串, 如下列的 ip 的值:

```java
xxl:
  job:
    admin:
      addresses: http://127.0.0.1:8080/xxl-job-admin  #xxljob调度中心部署
    executor:
      appname: sar-timing-xxlJob #xxljob配置的执行器名称，
      ip: ""            #执行器IP，默认为空表示自动获取IP
      port: 9999 #xxljob配置的端口号，默认为9999
      logpath: /data/xxl-job/jobhandler  #执行器运行日志文件存储磁盘路径
      logretentiondays: 30  #调度中心日志表数据保存天数，过期日志自动清理；限制大于等于7时生效，否则, 如-1，关闭自动清理功能
    accessToken: default_token
```

### 步骤二:执行器组件配置

```java
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

```

### 步骤三: 部署执行器

## 3.开发第一个任务“Hello World”--BEAN模式（方法形式）

### 步骤一：执行器中开发Job方法

```java
@Component
public class HelloWord {
    @XxlJob("helloWorld")
    public ReturnT<String> hellowWorld(String param) throws Exception {
        XxlJobLogger.log("XXLJOB-HelloWord start...");
        System.out.println("Hello Word!");
        return ReturnT.SUCCESS;
    }
}
```

>注意点:
>
>1.  @XxlJob("helloWorld"): @XxlJob的值是**JobHandler**的名称
>2.   public ReturnT<String> hellowWorld(String param)  方法标签:返回值和参数固定
>3. @Component注解: 定时任务要交给spring管理

### 步骤二: 新建任务

![image-20240411183159627](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240411183159627.png)

### 步骤三: 手动触发或启动

![image-20240416091517793](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240416091517793.png)





