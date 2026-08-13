package com.campuseatery.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminLocalhostFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/admin") || path.startsWith("/api/admin")) {
            if (!isLocalhost(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                if (path.startsWith("/api/")) {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"detail\":\"Forbidden: Admin portal is only accessible via localhost.\"}");
                } else {
                    response.setContentType("text/html");
                    response.getWriter().write("<!DOCTYPE html><html><head><title>403 Forbidden</title></head><body style=\"font-family: sans-serif; text-align: center; padding: 50px;\"><h1 style=\"color: #ef4444;\">403 Access Denied</h1><p>The Admin Portal is restricted to localhost access only.</p></body></html>");
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLocalhost(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String serverName = request.getServerName();

        if ("127.0.0.1".equals(remoteAddr) ||
            "0:0:0:0:0:0:0:1".equals(remoteAddr) ||
            "::1".equals(remoteAddr) ||
            "localhost".equalsIgnoreCase(remoteAddr) ||
            "localhost".equalsIgnoreCase(serverName) ||
            "127.0.0.1".equals(serverName)) {
            return true;
        }

        return false;
    }
}
