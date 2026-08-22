package com.campuseatery.config;

import com.campuseatery.model.User;
import com.campuseatery.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ClerkSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    
    // In-memory cache to avoid hitting the DB on every single request
    private final Map<String, Boolean> syncedUsers = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            String clerkId = jwt.getSubject();
            
            // Just-In-Time Provisioning
            if (!syncedUsers.containsKey(clerkId)) {
                User user = userRepository.findById(clerkId).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setId(clerkId);
                    
                    String email = null;
                    try {
                        if (jwt.hasClaim("email")) {
                            Object claim = jwt.getClaim("email");
                            email = claim != null ? claim.toString() : null;
                        } else if (jwt.hasClaim("primaryEmailAddress")) {
                            Object claim = jwt.getClaim("primaryEmailAddress");
                            email = claim != null ? claim.toString() : null;
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                    if (email == null) {
                        email = clerkId + "@clerk.placeholder.com";
                    }
                    
                    user.setEmail(email);
                    
                    String role = "customer";
                    Map<String, Object> metadata = null;
                    try {
                        Object metaObj = jwt.getClaim("publicMetadata");
                        if (metaObj == null) metaObj = jwt.getClaim("public_metadata");
                        if (metaObj instanceof Map) {
                            metadata = (Map<String, Object>) metaObj;
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                    
                    if (metadata != null && metadata.containsKey("role")) {
                        role = metadata.get("role").toString();
                    } else if (email != null && email.toLowerCase().contains("admin")) {
                        role = "admin";
                    } else if (email != null && email.toLowerCase().contains("vendor")) {
                        role = "vendor";
                    }
                    user.setRole(role);
                    if ("admin".equalsIgnoreCase(role) || "vendor".equalsIgnoreCase(role)) {
                        user.setApprovalStatus("approved");
                    }
                    
                    try {
                        userRepository.save(user);
                    } catch (Exception e) {
                        // Log and continue if save fails (e.g. duplicate key)
                        System.err.println("Failed to save new user in ClerkSyncFilter: " + e.getMessage());
                    }
                }
                syncedUsers.put(clerkId, true);
            }

            // Wrap request to inject X-User-Id header securely
            // This prevents the frontend from spoofing the header, overriding it with the verified JWT subject
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return clerkId;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(clerkId));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    if (!names.contains("X-User-Id")) {
                        names.add("X-User-Id");
                    }
                    return Collections.enumeration(names);
                }
            };
            
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
