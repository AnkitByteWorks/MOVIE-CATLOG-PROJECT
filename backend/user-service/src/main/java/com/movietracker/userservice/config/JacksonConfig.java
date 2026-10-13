package com.movietracker.userservice.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.jackson2.autoconfigure.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson JSON serialization and deserialization customization.
 */
@Configuration
public class JacksonConfig {

    /**
     * Customizes Spring Boot's Jackson ObjectMapper with JavaTimeModule, ISO-8601 date handling,
     * and omission of null fields from serialized JSON.
     *
     * @return customizer for {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder}
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
