package com.fpo.vendas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Vendas & Gerenciamento de Estoque API")
                        .version("1.0.0")
                        .description("Sistema back-end construído com Spring Boot 3, Hibernate, H2 Database, e princípios SOLID.")
                        .contact(new Contact()
                                .name("Equipe 3")
                                .email("andreybezerra.info@gmail.com")));
    }
}