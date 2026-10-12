package com.movietracker.movieservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 specification configuration for movie-service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movieServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Movie Tracker — Movie Catalog Service")
                        .description("RESTful microservice for movie & TV catalog discovery, TMDB synchronization, and cached trending content.")
                        .version("1.0.0")
                        .contact(new Contact().name("Movie Tracker Team").email("dev@movietracker.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
