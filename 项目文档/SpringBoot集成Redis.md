## SpringBoot集成Redis

## 1. 依赖	

```
<!-- 集成redis依赖  -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 2.配置

### 2.1 application.yaml

```
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 0
```

### 2.2 RedisConfig.class

> 配置序列化方式

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        // 创建模板
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置序列化工具
        GenericJackson2JsonRedisSerializer jsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer();
        // key和 hashKey采用 string序列化
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        // value和 hashValue采用 JSON序列化
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }
}
```

### 2.3  工具类

> redis中可以支持 string, list, hash,set, zset五种数据类型，这五种数据格式的常用API都在RedisTemplate这个类中进行了封装。为了方便开发，通常都会定义一个工具类，在使用的时候直接注入这个工具类

```java
package com.demo.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtil {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<String, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        boolean result = false;
        try {
            ValueOperations<String, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, timeUnit);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hmSet(String key, Object hashKey, Object value) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hmGet(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public void lPush(String k, Object v) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        list.rightPush(k, v);
    }

    /**
     * 列表获取
     *
     * @param k
     * @param l
     * @param l1
     * @return
     */
    public List<Object> lRange(String k, long l, long l1) {
        ListOperations<String, Object> list = redisTemplate.opsForList();
        return list.range(k, l, l1);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
    public void add(String key, Object value) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        set.add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key) {
        SetOperations<String, Object> set = redisTemplate.opsForSet();
        return set.members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
    public void zAdd(String key, Object value, double scoure) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        zset.add(key, value, scoure);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
    public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
        return zset.rangeByScore(key, scoure, scoure1);
    }

    /**
     * 利用redis自增获取唯一主键
     * @param key
     * @return
     */
    public long incrementAndGet(String key) {
        return redisTemplate.opsForValue().increment(key, 1L);
    }

    public Boolean setIfAbsent(String key, Object value, Long timeout, TimeUnit unit) {
        Boolean result = true;

        try {
            if (unit == null) {
                result = this.redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.MILLISECONDS);
            } else {
                result = this.redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
            }
        } catch (Exception var6) {
            log.error("redis加锁失败");
        }

        return result;
    }
}
```

## 3.应用

### 3.1 缓存

```java
    @Autowired
    RedisUtil redisUtil;
    
    @RequestMapping("/setByUtil")
    public CommonResponse<Object> setByUtil(){
        User user = new User();
        user.setId(5);
        user.setName("王五");
        user.setPhone(123124324);
        redisUtil.set(key+user.getId(), user, 24L, TimeUnit.HOURS);
        return CommonResponse.success(user, "success");
    }
```

### 3.2 自增主键

```java
    @RequestMapping("/setIncrement")
    public CommonResponse<Object> setIncrement(){
        String key = "demo:provider:seq";
        long l = redisUtil.incrementAndGet(key);
        return CommonResponse.success(l, "success");
    }
```

###  3.3 分布式锁

#### 3.3.1 RedisTemplate

```java
@Autowired
 RedisTemplate<String, Object> redisTemplate;
 //获得锁
 Boolean acquire = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS)
 //删除锁
 redisTemplate.delete(key[0]);
```

>可能出现的问题:
>
>- 方法未执行完成，锁过期的现象 
>- 没有获得锁的线程, 如果不想退出,需要手动实现自旋
>- 误删 key , 第二个线程刚刚获得到锁 ,  第一个线程删除了锁

可在定时任务中使用, 防止任务重复执行,将过期时间定的长一些,就不会有锁过期未执行完成和误删key的隐患

```java
 
@XxlJob("xxlJobHandler")
    public ReturnT<String> syncProToBasic(String param) {
        log.info("xxlJobHandler start");
        XxlJobLogger.log("xxlJobHandler start，时间为：{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        String key = "KEY_NAME";
        //防重复锁,15分钟
        Boolean acquire = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis(), 900L, TimeUnit.SECONDS);
        if (acquire) {
            try {
             //业务逻辑
             //....
            } catch (BizException e) {
                log.info("xxlJobHandler任务执行失败，原因：{}", e.getMessage());
                return ReturnT.FAIL;
            } finally {
                //解锁
                redisTemplate.delete(key);
            }
        } else {
            log.info("xxlJobHandler 加锁失败，不处理");
            return ReturnT.FAIL;
        }
        XxlJobLogger.log("xxlJobHandler end，时间为：{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        log.info("xxlJobHandler end");
        return ReturnT.SUCCESS;
    }
```

#### 3.3.2 Redisson 分布式锁

- 高并发时， 如果设置过期时间比较短，有时会出现方法未执行完成，锁过期的现象
  ression提供了"看门狗"机制，方法未执行完成会自动续期

##### 3.3.2.1 Redisson 依赖

```java
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.13.6</version>
</dependency>
```

##### 3.3.2.2  Redisson 配置

```java
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() throws IOException {
       //1、创建配置
        Config config = new Config();
        //使用单节点模式
        config.useSingleServer().setAddress("redis://XXXX:6379");

        //2、根据Config创建出RedissonClient实例
        //Redis url should start with redis:// or rediss://
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;  
    }
}
```

##### 3.3.2.3  Redisson 使用

```java
public CommonResponse<Object> redissonLock() {
        // 1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        // 加锁
        // 阻塞式等待，默认加的锁都是【看门狗时间】30s时间, 如果设置过期时间, 则没有续期机制
        //1)、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期被删掉
        //2)、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
        lock.lock();
        try {
            System.out.println("加锁成功......."+Thread.currentThread().getId());
            Thread.sleep(40000);
        } catch (InterruptedException e) {

        }finally {
            // 释放锁   不会出现死锁状态 如果没有执行解锁，锁有过期时间，过期了会将锁删除
            lock.unlock();
            System.out.println("解锁成功......"+Thread.currentThread().getId());
        }
        return CommonResponse.success(null, "占锁成功");
    }
```

