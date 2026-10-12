package com.movietracker.watchlog.client;

import com.movietracker.watchlog.config.FeignConfig;
import com.movietracker.watchlog.dto.external.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client communication contract for user-service.
 */
@FeignClient(
        name = "user-service",
        url = "${app.clients.user-service-url}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{username}")
    UserResponse getPublicProfile(@PathVariable("username") String username);

    @GetMapping("/api/v1/users/me")
    UserResponse getCurrentUser(@RequestHeader("Authorization") String auth);
}
