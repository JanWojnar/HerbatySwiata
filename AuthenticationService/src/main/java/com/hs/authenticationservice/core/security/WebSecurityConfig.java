package com.hs.authenticationservice.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) throws Exception {

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Disable CSRF because of state-less session-management
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.disable());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter())));

        http.authorizeHttpRequests(accessManagement -> accessManagement
                .requestMatchers("/auth/customer/login", "/auth/customer/register").permitAll()
                .anyRequest().fullyAuthenticated());

        http.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "OAuth realm=\"%s\"".formatted(issuerUri));
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
            log.error("Authentication failure: " + authException.getMessage(), authException);
        }));
        
        return http.build();
    }
}
