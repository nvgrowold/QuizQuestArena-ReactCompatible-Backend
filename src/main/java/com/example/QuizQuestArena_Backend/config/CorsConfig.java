package com.example.QuizQuestArena_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//mark the class as a configuration class
public class CorsConfig { //Cross-Origin Resource Sharing

    @Bean //this annotation returns a Spring-managed bean. Here it returns a WeMvcConfigurer object to configure CORS mappings
    public WebMvcConfigurer corsConfigurer(){ //WeMvcConfigurer Interface: define custom CORS mappings
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) { //customize how this app interacts with requests from different origions
                registry.addMapping("/**") //Allows CORS for all endpoints in the app
                        .allowedMethods("GET","POST","PUT","DELETE") //specifies the allowed HTTP methods for cross-origin request
                        .allowedHeaders("*") //allows all headers in CORS request
                        .allowedOrigins("http://localhost:3000") // Explicitly specify the frontend origin
                        .allowCredentials(true); // Allow credentials (cookies, sessions, etc.)
            }
        };
    }
}

