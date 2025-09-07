package com.multibank.notification_routing.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    private final String redisServerUrl;

    public RedisConfig(@Value("${redis.server.url}") String redisServerUrl) {
        this.redisServerUrl = redisServerUrl;
    }

    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisServerUrl);
        return Redisson.create(config);
    }

}