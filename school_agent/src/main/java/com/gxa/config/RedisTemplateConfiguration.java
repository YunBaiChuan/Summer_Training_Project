package com.gxa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author zxd
 * @date 2026/1/17
 */
@Configuration
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // key 序列化方式 采用 StringRedisSerializer
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // value 序列化 采用 json 序列化
        redisTemplate.setValueSerializer(RedisSerializer.json());

        // hash key 序列化方式 采用 StringRedisSerializer
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        // hash value 序列化方式 采用 json 序列化
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        // 设置数据库序列化方式
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
