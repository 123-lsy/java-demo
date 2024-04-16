package com.demo.provider.controller;

import com.demo.common.util.CommonResponse;
import com.demo.provider.config.RedisUtil;
import com.demo.provider.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@RestController
@RequestMapping("/demo/redis")
@Slf4j
public class RedisController {

    @Autowired
    RedissonClient redissonClient;


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String key = "demo:provider:user:";
    private static final String key1 = "demo:provider:lock";

    @RequestMapping("/setByTemplate")
    public CommonResponse<Object> setByTemplate(){
        User user = new User();
        user.setId(4);
        user.setName("李四");
        user.setPhone(123124324);
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(key+user.getId(), user, 60L, TimeUnit.SECONDS);
        return CommonResponse.success(user, "success");
    }

    /**
     * 缓存
     * @return
     */
    @RequestMapping("/setByUtil")
    public CommonResponse<Object> setByUtil(){
        User user = new User();
        user.setId(5);
        user.setName("王五");
        user.setPhone(123124324);
        redisUtil.set(key+user.getId(), user, 24L, TimeUnit.HOURS);
        return CommonResponse.success(user, "success");
    }
    @RequestMapping("/getByUtil")
    public CommonResponse<Object> getByUtil(Integer id){
        return CommonResponse.success(redisUtil.get(key + id), "success");
    }

    /**
     * redis自增 生成唯一id
     * @return
     */
    @RequestMapping("/setIncrement")
    public CommonResponse<Object> setIncrement(){
        String key = "demo:provider:seq";
        long l = redisUtil.incrementAndGet(key);
        return CommonResponse.success(l, "success");
    }

    /**
     * redisTemplate 占用的锁可以用来防止定时任务重复执行
     * @return
     */
    @RequestMapping("/lock")
    public CommonResponse<Object> lock(){
        Boolean acquire = redisUtil.setIfAbsent(key1, System.currentTimeMillis(), 30L, TimeUnit.SECONDS);
        if (acquire){
            try {
                sleep(50000L);
            } catch (Exception e) {
               log.info(e.getMessage());
            }finally {
                redisUtil.remove(key1);
            }
        }else {
            return CommonResponse.fail("占锁失败");
        }
        return CommonResponse.success(null, "success");
    }


    @RequestMapping("/redissonLock")
    public CommonResponse<Object> redissonLock() {
        // 1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        // 加锁
        // 阻塞式等待，默认加的锁都是【看门狗时间】30s时间
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

}
