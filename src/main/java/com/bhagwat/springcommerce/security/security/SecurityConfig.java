package com.bhagwat.springcommerce.security.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/products/**")

                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/cart/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/orders")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/orders/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/orders/*/cancel")
                        .authenticated()

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/orders/*/status")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/*/reviews")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products/*/reviews")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/reviews/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/reviews/**")
                        .authenticated()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

}