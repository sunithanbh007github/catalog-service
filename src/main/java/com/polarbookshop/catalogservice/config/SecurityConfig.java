package com.polarbookshop.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;


@Configuration
@EnableWebSecurity //Enables Spring MVC support for Spring Security
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/books/**").permitAll() //Allows users to fetch greetings and books without being authenticated
                        //.anyRequest().authenticated() //Any other request requires authentication.
                        .anyRequest().hasRole("employee") //Any other request requires not only authentication but also the employee role (which is the same as the ROLE_employee authority).
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())) //Enables OAuth2 Resource Server support using the default configuration based on JWT
                .sessionManagement(sessionManagement -> //Each request must include an Access Token, so there’s no need to keep a user session alive between requests. We want it to be stateless.
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable) //Since the authentication strategy is stateless and doesn’t involve a browser-based client, we can safely disable the CSRF protection.
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter(); //Defines a converter to map claims to GrantedAuthority objects
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); //Applies the “ROLE_” prefix to each user role
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); //Extracts the list of roles from the roles claim

        var jwtAuthenticationConverter = new JwtAuthenticationConverter(); //Defines a strategy to convert a JWT.
                                                                           //We’ll only customize how to build granted authorities out of it.
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}

