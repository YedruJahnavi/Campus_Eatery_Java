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
        // Map clean URLs to their corresponding HTML files
        registry.addViewController("/vendors").setViewName("forward:/vendors.html");
        registry.addViewController("/cart").setViewName("forward:/cart.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/menu").setViewName("forward:/menu.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/signup").setViewName("forward:/index.html");
    }
}
