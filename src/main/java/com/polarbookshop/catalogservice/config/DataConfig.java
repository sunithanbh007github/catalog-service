package com.polarbookshop.catalogservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJdbcAuditing //Enables entity auditing in Spring Data JDBC
public class DataConfig {

    @Bean
    AuditorAware<String> auditorAware() { //Returns the currently authenticated user for auditing purposes

        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                //Extracts the SecurityContext object for the currently authenticated user from SecurityContextHolder
                .map(SecurityContext::getAuthentication)
                //Extracts the Authentication object for the currently authenticated user from SecurityContext
                .filter(Authentication::isAuthenticated)
                //Handles the case where a user is not authenticated, but is manipulating data. Since we protected all
                //the endpoints, this case should never happen, but we’ll include it for completeness.
                .map(Authentication::getName);
                //Extracts the username for the currently authenticated user from the Authentication object
    }
}
