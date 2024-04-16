# SpringBoot集成Nacos和Open Feign 

## 前置知识

### 一、创建 SpringBoot 工程

①. 创建Maven工程 - Spring Initializer在社区版本是收费的

社区版本的 IDEA 创建 Spring Boot 工程可选取 maven-archetype-quickstart 快速搭建


![image-20240416105349266](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240416105349266.png)

②. 导入spring-boot-stater-web起步依赖

 ```java
<dependency>  
<groupId>org.springframework.boot</groupId>   
<artifactId>spring-boot-starter-web</artifactId>
</dependency>
 ```

③.调整工程架构并提供启动类

![ 	](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240416110057588.png)![image-20240416111115930](C:\Users\mi\AppData\Roaming\Typora\typora-user-images\image-20240416111115930.png)



④. 提供启动类

```java
//启动类
@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

#### 二、配置文件的书写和获取

1.配置信息书写

- resource目录下方

- 值前边必须有空格，作为分隔符

- 使用空格作为缩进表示层级关系，相同的层级左侧对齐
- 配置文件加载顺序(由先到后):bootstrap.properties -> bootstrap.yml -> application.properties -> application.yml
  如果有相同 key ，后面加载的配置会覆盖前面的

2.配置信息获取

- @Value(“${键名}”)
- @ConfigurationProperties(prefix = “前缀”)

### 三、Bean管理

#### Bean注册

- 自定义的Bean

| 注解        | 说明                 | 位置                                            |
| ----------- | -------------------- | ----------------------------------------------- |
| @Component  | 声明bean的基础注解   | 不属于以下三类时，用此注解                      |
| @Controller | @Component的衍生注解 | 标注在控制器类上                                |
| @Service    | @Component的衍生注解 | 标注在业务类上                                  |
| @Repository | @Component的衍生注解 | 标注在数据访问类上（由于与mybatis整合，用的少） |

- 第三方的Bean（如果要注册的bean对象来自于第三方（不是自定义的），是无法用 @Component 及衍生注解声明bean的）

  ```java
  @Configuration
  public class XxlJobConfig {
      @Bean
      public XxlJobSpringExecutor xxlJobExecutor() {
          return new XxlJobSpringExecutor;
      }
  }
  
  ```

## 1.nacos作为注册中心

### 步骤1：

在pom文件的依赖管理中加入 spring cloud alibaba 的依赖管理 这样引入相关依赖的时候就不用加版本了；

```java
  <dependencyManagement>
        <dependencies>
            <!-- 定义 spring cloud alibaba-->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba-dependencies.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

```
   <dependencies>
        <!--nacos服务注册与发现-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-nacos-discovery</artifactId>
        </dependency>
    </dependencies>
```

### 步骤2：

在启动类上加 `@EnableDiscoveryClient`注解
```java
@SpringBootApplication
@EnableDiscoveryClient
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class);
    }
}
```

### 步骤3：

在application.yml 下加配置
```java
spring:
  application:
    name: demo-service
  cloud:
    nacos:
      server-addr: localhost:8848
```

## 2.nacos作为配置中心

统一配置


### 步骤1：在nacos中心创建配置

- 命名空间：区分开发、测试和生产环境,环境配置隔离

- dateId: ${application.name}-${profile}.${file-extension}
- group:做同一个环境中的不同服务分组,不同业务配置隔离

### 步骤2：引入nacos配置依赖

```java
 <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-nacos-config</artifactId>
 </dependency>
```

### 步骤3：添加 bootstrap.yml文件

bootstrap.yml文件是引导文件，比application.yml先加载

```java
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: ee9a7297-5785-4396-8568-3e2d390fa979
        group: java-demo
        file-extension: yaml
        refresh-enabled: true
      discovery:
        server-addr: localhost:8848
        namespace: ee9a7297-5785-4396-8568-3e2d390fa979
        group: java-demo
  application:
    name: demo-provider
```

nacos的配置是会覆盖本地配置的

## 3.open feign 远程调用

>声明式远程调用,需要与注册中心搭配使用,所以调用的服务也需要注册到nacos
>
>feign是一个声明式的HTTP客户端，他的目的就是让远程调用更加简单。给远程服务发的是HTTP请求。

### 步骤1:引入依赖

```java
       <!--        open feign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```

### 步骤2:启动类上加 `@EnableFeignClients`

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class);
    }
}
```

  ### 步骤三: feign包下新建接口

```java
@FeignClient("demo-provider") //告诉spring cloud这个接口是一个远程客户端，要调用demo-provider服务(nacos中找到)，具体是调demo-provider服务的/demo/provider/getUser对应的方法
public interface UserFeignService {
    // 远程服务的url
    @RequestMapping("/demo/provider/getUser")//注意写全优惠券类上还有映射//注意我们这个地方不是控制层，所以这个请求映射请求的不是我们服务器上的东西，而是nacos注册中心的
     CommonResponse<Object> getUser();;//得到一个CommonResponse对象
}
```

### 步骤4: 注入接口

```java
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
```

