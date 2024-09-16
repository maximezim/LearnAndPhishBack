package com.learnandphish.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Maxime Zimmermann
 */
@Configuration
public class GatewayConfig {
//    private final RequestFilter requestFilter;
    private final AuthFilter authFilter;

    public GatewayConfig(RequestFilter requestFilter, AuthFilter authFilter) {
        // this.requestFilter = requestFilter;
        this.authFilter = authFilter;
    }

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("first-microservice", r -> r.path("/first")
//                        .and().method("POST")
//                        .and().readBody(Student.class, s -> true)
//                        .filters(f -> f.filters(requestFilter, authFilter))
//                        .uri("http://localhost:8081"))
//                .route("first-microservice", r -> r.path("/first")
//                        .and().method("GET")
//                        .filters(f -> f.filters(authFilter))
//                        .uri("http://localhost:8081"))
//                .route("second-microservice", r -> r.path("/second")
//                        .and().method("POST")
//                        .and().readBody(Company.class, s -> true)
//                        .filters(f -> f.filters(requestFilter, authFilter))
//                        .uri("http://localhost:8082"))
//                .route("second-microservice", r -> r.path("/second")
//                        .and().method("GET")
//                        .filters(f -> f.filters(authFilter))
//                        .uri("http://localhost:8082"))
//                .route("auth-server", r -> r.path("/login")
//                        .uri("http://localhost:8088"))
//                .build();
//    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/authenticate")
                        .uri("http://authentication-service:8082"))
                .route("auth-swagger", r -> r.path("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**")
                        .uri("http://authentication-service:8082"))
                .route("protected-routes", r -> r.path("/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://authentication-service:8082"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:5173")); // Allow only this origin
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}