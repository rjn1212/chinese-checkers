package com.gitgames.chinesecheckers.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class PlatformAuthFilter implements Filter {

    @Value("${platform.jwt.secret:}")
    private String jwtSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String authHeader = httpReq.getHeader("Authorization");

        if (jwtSecret != null && !jwtSecret.isBlank() && authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret))
                        .build()
                        .verify(token);
                httpReq.setAttribute("platformUserId", jwt.getSubject());
                httpReq.setAttribute("platformUserEmail", jwt.getClaim("email").asString());
                httpReq.setAttribute("platformUserRole", jwt.getClaim("role").asString());
            } catch (Exception ignored) {
            }
        }

        chain.doFilter(request, response);
    }
}
