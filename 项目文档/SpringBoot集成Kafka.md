SpringBoot集成Kafka

## 1.引入依赖

```java
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## 2.配置

有config和yaml两种方式  但是有的配置只能用config配置 比如不同服务的kafka消息 类似于多数据源配置

### 生产者

- yaml

```yaml
spring:
  kafka:
    producer:
      # Kafka服务器
      bootstrap-servers: 175.24.228.202:9092
      # 开启事务，必须在开启了事务的方法中发送，否则报错
      # transaction-id-prefix: kafkaTx-
      # 发生错误后，消息重发的次数，开启事务必须设置大于0。
      retries: 3
      # acks=0 ： 生产者在成功写入消息之前不会等待任何来自服务器的响应。
      # acks=1 ： 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应。
      # acks=all ：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。
      # 开启事务时，必须设置为all
      acks: all
      # 当有多个消息需要被发送到同一个分区时，生产者会把它们放在同一个批次里。该参数指定了一个批次可以使用的内存大小，按照字节数计算。
      batch-size: 16384
      # 生产者内存缓冲区的大小。
      buffer-memory: 1024000
      # 键的序列化方式
      key-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      # 值的序列化方式（建议使用Json，这种序列化方式可以无需额外配置传输实体类）
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

- config

```java
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka配置，也可以写在yml，这个文件会覆盖yml
 */
@SpringBootConfiguration
public class KafkaProviderConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.transaction-id-prefix}")
    private String transactionIdPrefix;
    @Value("${spring.kafka.producer.acks}")
    private String acks;
    @Value("${spring.kafka.producer.retries}")
    private String retries;
    @Value("${spring.kafka.producer.batch-size}")
    private String batchSize;
    @Value("${spring.kafka.producer.buffer-memory}")
    private String bufferMemory;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        //acks=0 ： 生产者在成功写入消息之前不会等待任何来自服务器的响应。
        //acks=1 ： 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应。
        //acks=all ：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。
        //开启事务必须设为all
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        //发生错误后，消息重发的次数，开启事务必须大于0
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        //当多个消息发送到相同分区时,生产者会将消息打包到一起,以减少请求交互. 而不是一条条发送
        //批次的大小可以通过batch.size 参数设置.默认是16KB
        //较小的批次大小有可能降低吞吐量（批次大小为0则完全禁用批处理）。
        //比如说，kafka里的消息5秒钟Batch才凑满了16KB，才能发送出去。那这些消息的延迟就是5秒钟
        //实测batchSize这个参数没有用
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        //有的时刻消息比较少,过了很久,比如5min也没有凑够16KB,这样延时就很大,所以需要一个参数. 再设置一个时间,到了这个时间,
        //即使数据没达到16KB,也将这个批次发送出去
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5000");
        //生产者内存缓冲区的大小
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        //反序列化，和生产者的序列化方式对应
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        //开启事务，会导致 LINGER_MS_CONFIG 配置失效
        factory.setTransactionIdPrefix(transactionIdPrefix);
        return factory;
    }

    @Bean
    public KafkaTransactionManager<Object, Object> kafkaTransactionManager(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### 消费者

- yaml

```yaml
server:
  port: 8082
spring:
  kafka:
    consumer:
      # Kafka服务器
      bootstrap-servers: 175.24.228.202:9092
      group-id: firstGroup
      # 该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理：
      # earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费分区的记录
      # latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据（在消费者启动之后生成的记录）
      # none：当各分区都存在已提交的offset时，从提交的offset开始消费；只要有一个分区不存在已提交的offset，则抛出异常
      auto-offset-reset: latest
      enable-auto-commit: false
      key-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      # 这个参数定义了poll方法最多可以拉取多少条消息，默认值为500。
      # 如果消费者无法在5分钟内处理完500条消息的话就会触发reBalance,
      # 然后这批消息会被分配到另一个消费者中，还是会处理不完，这样这批消息就永远也处理不完。
      # 要避免出现上述问题，提前评估好处理一条消息最长需要多少时间，然后覆盖默认的max.poll.records参数
      # 注：需要开启BatchListener批量监听才会生效，如果不开启BatchListener则不会出现reBalance情况
      max-poll-records: 3
    properties:
      # 两次poll之间的最大间隔，默认值为5分钟。如果超过这个间隔会触发reBalance
      max:
        poll:
          interval:
            ms: 600000
      # 当broker多久没有收到consumer的心跳请求后就触发reBalance，默认值是10s
      session:
        timeout:
          ms: 10000
    listener:
      # 在侦听器容器中运行的线程数，一般设置为 机器数*分区数
      concurrency: 4
      # 自动提交关闭，需要设置手动消息确认
      ack-mode: manual_immediate
      # 消费监听接口监听的主题不存在时，默认会报错，所以设置为false忽略错误
      missing-topics-fatal: false
      # 两次poll之间的最大间隔，默认值为5分钟。如果超过这个间隔会触发reBalance
      poll-timeout: 600000

```

- config

```java
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 徐一杰
 * @date 2022/10/31 18:05
 * kafka配置，也可以写在yml，这个文件会覆盖yml
 */
@SpringBootConfiguration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;
    @Value("${spring.kafka.properties.session.timeout.ms}")
    private String sessionTimeout;
    @Value("${spring.kafka.properties.max.poll.interval.ms}")
    private String maxPollIntervalTime;
    @Value("${spring.kafka.consumer.max-poll-records}")
    private String maxPollRecords;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    @Value("${spring.kafka.listener.concurrency}")
    private Integer concurrency;
    @Value("${spring.kafka.listener.missing-topics-fatal}")
    private boolean missingTopicsFatal;
    @Value("${spring.kafka.listener.poll-timeout}")
    private long pollTimeout;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>(16);
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        //是否自动提交偏移量，默认值是true，为了避免出现重复数据和数据丢失，可以把它设置为false，然后手动提交偏移量
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        //自动提交的时间间隔，自动提交开启时生效
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "2000");
        //该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理：
        //earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费分区的记录
        //latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据（在消费者启动之后生成的记录）
        //none：当各分区都存在已提交的offset时，从提交的offset开始消费；只要有一个分区不存在已提交的offset，则抛出异常
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        //两次poll之间的最大间隔，默认值为5分钟。如果超过这个间隔会触发reBalance
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalTime);
        //这个参数定义了poll方法最多可以拉取多少条消息，默认值为500。如果在拉取消息的时候新消息不足500条，那有多少返回多少；如果超过500条，每次只返回500。
        //这个默认值在有些场景下太大，有些场景很难保证能够在5min内处理完500条消息，
        //如果消费者无法在5分钟内处理完500条消息的话就会触发reBalance,
        //然后这批消息会被分配到另一个消费者中，还是会处理不完，这样这批消息就永远也处理不完。
        //要避免出现上述问题，提前评估好处理一条消息最长需要多少时间，然后覆盖默认的max.poll.records参数
        //注：需要开启BatchListener批量监听才会生效，如果不开启BatchListener则不会出现reBalance情况
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        //当broker多久没有收到consumer的心跳请求后就触发reBalance，默认值是10s
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        //序列化（建议使用Json，这种序列化方式可以无需额外配置传输实体类）
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return propsMap;
    }

    @Bean
    public ConsumerFactory<Object, Object> consumerFactory() {
        //配置消费者的 Json 反序列化的可信赖包，反序列化实体类需要
        try(JsonDeserializer<Object> deserializer = new JsonDeserializer<>()) {
            deserializer.trustedPackages("*");
            return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new JsonDeserializer<>(), deserializer);
        }
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Object, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        //在侦听器容器中运行的线程数，一般设置为 机器数*分区数
        factory.setConcurrency(concurrency);
        //消费监听接口监听的主题不存在时，默认会报错，所以设置为false忽略错误
        factory.setMissingTopicsFatal(missingTopicsFatal);
        //自动提交关闭，需要设置手动消息确认
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(pollTimeout);
        //设置为批量监听，需要用List接收
        //factory.setBatchListener(true);
        return factory;
    }
}
```

## 3.简单应用

- 简单生产消息

  ```java
      @Autowired
      private KafkaTemplate<Object, Object> kafkaTemplate;
  
      @GetMapping("/send")
      public CommonResponse<Object> sender(){
          String msg = "hello world!";
          kafkaTemplate.send("test", msg);
          return CommonResponse.success(null, "success");
      }
  ```

- 简单消费消息

  ```java
      @KafkaListener(topics = {"test"})
      public void listen1(ConsumerRecord<String, Object> consumerRecord, Acknowledgment ack) {
          try {
              //用于测试异常处理
              //int i = 1 / 0;
              System.out.println(consumerRecord);
          } catch (Exception e) {
              System.out.println("消费失败：" + e);
          }finally {
              //手动确认
              ack.acknowledge();
          }
      }
  ```

### 3.1 消息不丢失--回调/重试/确认

```
    @Transactional
    @GetMapping("/send/callback")
    public CommonResponse<Object> callback(){
        String msg = "hello world!";
        kafkaTemplate.send("test", msg).addCallback(new SuccessCallback<SendResult<Object, Object>>() {
            @Override
            public void onSuccess(SendResult<Object, Object> objectObjectSendResult) {
                log.info("消息发送成功");
            }
        }, new FailureCallback() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("消息发送失败:{}, topic:{}, msg:{}", throwable.getCause(), "test", msg);
                //补偿机制
            }
        });
        return CommonResponse.success(null, "success");

    }
```

### 3.2 消息事务

```
      # 开启事务，必须在开启了事务的方法中发送，否则报错
      # transaction-id-prefix: kafkaTx-
```

- 方法一:

  ```java
      @GetMapping("/send/transaction")
      public CommonResponse<Object> transaction(){
          kafkaTemplate.executeInTransaction(operations -> {
            operations.send("test", "kafka事务消息发送");
            int i  = 1/0;
            operations.send("test", "kafka事务消息发送2");
            return true;
          });
          return CommonResponse.success(null, "success");
      }
  ```

- 方法二: 注入KafkaTransactionManager

  ```
      @Bean
      public ProducerFactory<Object, Object> producerFactory() {
          DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
          //开启事务，会导致 LINGER_MS_CONFIG 配置失效
         // factory.setTransactionIdPrefix(transactionIdPrefix);
          return factory;
      }
  
      @Bean
      public KafkaTransactionManager<Object, Object> kafkaTransactionManager(ProducerFactory<Object, Object> producerFactory) {
          return new KafkaTransactionManager<>(producerFactory);
      }
  ```

  然后在方法上加上@Transactional
  ```
      @Transactional
      @GetMapping("/send")
      public CommonResponse<Object> sender(){
          String msg = "hello world!";
          kafkaTemplate.send("test", msg);
          return CommonResponse.success(null, "success");
      }
  ```

  
