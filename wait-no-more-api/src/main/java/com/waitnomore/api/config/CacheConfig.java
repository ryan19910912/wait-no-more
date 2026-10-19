package com.waitnomore.api.config;

import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // 定義不同的緩存桶子
        cacheManager.setCacheNames(Arrays.asList(
            "hospitals",    // 存醫院靜態資料
            "mrt_stations", // 存捷運站座標
            "categories"    // 存科別
        ));

        return cacheManager;
    }
}
