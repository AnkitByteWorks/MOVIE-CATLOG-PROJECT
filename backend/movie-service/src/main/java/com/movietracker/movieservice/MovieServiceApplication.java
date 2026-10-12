package com.movietracker.movieservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entrypoint for Movie Tracker Movie Service.
 * High-performance movie &amp; TV catalog service powered by MongoDB text search and Redis caching.
 */
@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient
@EnableMongoAuditing
@EnableScheduling
public class MovieServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieServiceApplication.class, args);
    }
}
