package com.teleport.number_tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(connectionFactory);
        return t;
    }

}
