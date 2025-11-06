package com.sucheth.riskwatch.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI riskWatchOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RiskWatch API")
                .description("RiskWatch is a modular API for real-time transaction risk evaluation and user risk profiling.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Sucheth Hegde")
                    .url("https://github.com/SuchethHegde"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(new Server().url("https://localhost:8080").description("Local dev server")
            ));
    }
}