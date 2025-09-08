package com.example.g7_back_mobile.controllers.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API G7 Back Mobile")
                        .description("API para aplicación móvil")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("G7 Team"))
                        .license(new License()
                                .name("Licencia API")
                                .url("http://www.ejemplo.com/licencias")));
    }
}