package com.FreeBoard.auth_proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Разрешить для всех путей
                        .allowedOrigins("*") // Разрешить для всех доменов
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Методы
                        .allowedHeaders("*") // Все заголовки
                        .allowCredentials(false); // Отключить передачу cookie
            }
        };
    }
}
