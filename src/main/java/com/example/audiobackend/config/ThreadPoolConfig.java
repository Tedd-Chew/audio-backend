package com.example.audiobackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
            2,          // 核心线程
            5,          // 最大线程
            60,         // 空闲时间
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100)  // 队列
        );
    }
    
}
