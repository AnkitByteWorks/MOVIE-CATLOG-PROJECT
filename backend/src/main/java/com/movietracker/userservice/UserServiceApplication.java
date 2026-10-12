package com.movietracker.userservice;

import com.movietracker.userservice.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Arrays;

/**
 * Entry point and configuration bootstrap for the Movie Tracker User Service.
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AppProperties.class)
public class UserServiceApplication {

    /**
     * Application entry point that starts the Spring application context and logs startup environment details.
     *
     * @param args command line arguments passed during launch
     */
    public static void main(String[] args) {
        var context = SpringApplication.run(UserServiceApplication.class, args);
        Environment env = context.getEnvironment();
        String appName = env.getProperty("spring.application.name", "user-service");
        String[] activeProfiles = env.getActiveProfiles();
        String profiles = activeProfiles.length == 0 ? "default" : Arrays.toString(activeProfiles);
        log.info("Application '{}' started successfully with active profile(s): {}", appName, profiles);
    }
}
