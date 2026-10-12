package com.movietracker.watchlog.config;

import com.movietracker.watchlog.exception.ExternalServiceException;
import com.movietracker.watchlog.exception.ResourceNotFoundException;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign client configuration enabling request header forwarding, logging, and error decoding.
 */
@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Forwards incoming Bearer Authorization header to downstream microservices when present.
     */
    @Bean
    public RequestInterceptor authorizationHeaderForwardingInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isBlank() && !template.headers().containsKey("Authorization")) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }

    /**
     * Maps downstream 404 responses to ResourceNotFoundException and other errors to ExternalServiceException.
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            log.warn("Feign call failed for method {} with status {}", methodKey, status);
            if (status == 404) {
                return new ResourceNotFoundException("External Resource", "method", methodKey);
            }
            return new ExternalServiceException("Downstream call failed: " + methodKey + " (status " + status + ")");
        };
    }
}
