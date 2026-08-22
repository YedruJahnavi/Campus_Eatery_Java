package com.campuseatery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClerkSyncFilter clerkSyncFilter;
    private final AdminLocalhostFilter adminLocalhostFilter;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> metadata = jwt.getClaim("publicMetadata");
            if (metadata == null) {
                metadata = jwt.getClaim("public_metadata");
            }
            if (metadata != null && metadata.containsKey("role")) {
                String role = (String) metadata.get("role");
                return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
            return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/admin/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/delivery/**").hasRole("DELIVERY")
                .requestMatchers(HttpMethod.GET, "/api/vendors/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/config").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recommendations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // Frontend static files
                .requestMatchers("/", "/index.html", "/dashboard.html", "/admin.html", "/vendor.html", "/checkout.html", "/css/**", "/js/**", "/assets/**", "/favicon.ico", "/*.html", "/*.css", "/*.js").permitAll()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .addFilterBefore(adminLocalhostFilter, org.springframework.security.web.header.HeaderWriterFilter.class)
            .addFilterAfter(clerkSyncFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder(
            @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri) {
        if (issuerUri == null || issuerUri.isBlank()) {
            // Provide a dummy decoder so the SecurityFilterChain bean doesn't fail to load when secrets are missing
            return token -> {
                throw new org.springframework.security.oauth2.jwt.JwtException("JWT processing is disabled because CLERK_ISSUER_URI is not configured.");
            };
        }
        return org.springframework.security.oauth2.jwt.JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
