package com.tastyhouse.webapi.config;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정
 * API 문서: <a href="http://localhost:8080/swagger-ui.html">swagger-ui.html</a>
 * OpenAPI JSON: <a href="http://localhost:8080/v3/api-docs">v3/api-docs</a>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI webApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TastyHouse Web API")
                        .description("TastyHouse 클라이언트용 공개 API 문서")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TastyHouse Team")
                                .email("support@tastyhouse.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.tastyhouse.com")
                                .description("운영 서버")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}