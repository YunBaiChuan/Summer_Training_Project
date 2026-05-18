package com.gxa.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author zxd
 * @date 2026/1/17
 */
@SpringBootTest
public class TestSpringRedis {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testSpringRedis(){

        //设置string类型的值
        redisTemplate.boundValueOps("aa").set("bbb");

        //获取string类型的值
        String aa = (String) redisTemplate.boundValueOps("aa").get();
        System.out.println(aa);

    }

}
