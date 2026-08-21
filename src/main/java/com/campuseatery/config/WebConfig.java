package com.campuseatery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map known React routes to index.html to avoid Spring Boot 3 PathPatternParser errors with /**
        String[] spaRoutes = {
            "/admin", "/admin/**", 
            "/dashboard", "/dashboard/**", 
            "/login", "/signup", "/cart", 
            "/vendors", "/vendors/**", 
            "/onboarding", "/orders", "/verify"
        };
        
        for (String route : spaRoutes) {
            registry.addViewController(route).setViewName("forward:/index.html");
        }
    }
}
