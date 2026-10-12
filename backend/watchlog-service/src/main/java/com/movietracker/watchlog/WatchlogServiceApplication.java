package com.movietracker.watchlog;

import com.movietracker.watchlog.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application bootstrap for Movie Tracker Watch Log Service.
 * Provides movie and television watch history tracking, viewing statistics, and Kafka event streaming.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(AppProperties.class)
public class WatchlogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatchlogServiceApplication.class, args);
    }
}
