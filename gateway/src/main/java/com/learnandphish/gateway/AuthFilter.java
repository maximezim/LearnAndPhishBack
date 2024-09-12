package com.learnandphish.gateway;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author Maxime Zimmermann
 */
@Component
@RefreshScope
public class AuthFilter implements GatewayFilter {

    @Autowired
    RouteValidator routeValidator;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthUtil authUtil;

    @Value("${authentication.enabled}")
    private boolean authEnabled;

    @Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    
    if (request.getURI().getPath().equals("/authenticate")) {
        return chain.filter(exchange);
    }

    if (!authEnabled) {
        System.out.println("Authentication is disabled. To enable it, make \"authentication.enabled\" property as true");
        return chain.filter(exchange);
    }

    if (routeValidator.isSecured.test(request)) {
        if (this.isCredsMissing(request)) {
            return this.onError(exchange, "Credentials missing", HttpStatus.UNAUTHORIZED);
        }

        String token = this.getAuthHeader(request);

        if (jwtUtil.isInvalid(token)) {
            return this.onError(exchange, "Auth header invalid", HttpStatus.UNAUTHORIZED);
        }

        this.populateRequestWithHeaders(exchange, token);
    }
    return chain.filter(exchange);
}

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return  request.getHeaders().getOrEmpty("Authorization").get(0);
    }


    private boolean isCredsMissing(ServerHttpRequest request) {
        return !(request.getHeaders().containsKey("userName") && request.getHeaders().containsKey("role")) && !request.getHeaders().containsKey("Authorization");
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.getAllClaims(token);
        exchange.getRequest()
                .mutate()
                .header("id",String.valueOf(claims.get("id")))
                .header("role", String.valueOf(claims.get("role")))
                .build();
    }
}
