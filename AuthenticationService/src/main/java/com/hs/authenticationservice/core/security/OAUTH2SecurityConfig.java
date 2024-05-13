package com.hs.authenticationservice.core.security;

import org.springframework.context.annotation.Configuration;


@Configuration
public class OAUTH2SecurityConfig {

    //TODO CONFIG requestów przychodzących
//    @Bean
//    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers(new AntPathRequestMatcher("/customers*", HttpMethod.OPTIONS.name()))
//                .permitAll()
//                .requestMatchers(new AntPathRequestMatcher("/customers*"))
//                .hasRole("user")
//                .requestMatchers(new AntPathRequestMatcher("/"))
//                .permitAll()
//                .anyRequest()
//                .authenticated());
//        http.oauth2ResourceServer((oauth2) -> oauth2
//                .jwt(Customizer.withDefaults()));
//        http.oauth2Login(Customizer.withDefaults())
//                .logout(logout -> logout.addLogoutHandler(keycloakLogoutHandler).logoutSuccessUrl("/"));
//        return http.build();
//    }
}
