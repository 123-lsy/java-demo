# SpringBoot集成nacos

## 前置知识

### 一、创建 SpringBoot 工程

①. 创建Maven工程

②. 导入spring-boot-stater-web起步依赖

③. 编写Controller 

④. 提供启动类

#### spring boot 起步依赖-maven坐标

 ```
 <dependency>    
 <groupId>org.springframework.boot</groupId>    
 <artifactId>spring-boot-starter-web</artifactId>
 </dependency>
 ```

#### 启动类

```java
//启动类
@SpringBootApplication
public class SpringbootQuickstartApplication {  
          public static void main(String[] args) {            SpringApplication.run(SpringbootQuickstartApplication.class, args);
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

- @Value(“${键名}”)-
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
