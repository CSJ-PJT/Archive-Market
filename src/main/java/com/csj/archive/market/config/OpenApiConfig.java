package com.csj.archive.market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI marketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Archive-Market API")
                .version("0.1.0")
                .description("Synthetic commerce backend for Archive Platform Ecosystem."));
    }
}
