package com.tinnova.veiculos.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain( HttpSecurity http )
        throws Exception {

        http.csrf( AbstractHttpConfigurer::disable )
            .authorizeHttpRequests(
                auth -> auth.requestMatchers( "/auth/**" ).permitAll().requestMatchers( "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html" ).permitAll()
                    .requestMatchers( "/actuator/**" ).permitAll().requestMatchers( HttpMethod.GET, "/veiculos/**" ).hasAnyRole( "USER", "ADMIN" )
                    .requestMatchers( HttpMethod.POST, "/veiculos/**" ).hasRole( "ADMIN" ).requestMatchers( HttpMethod.PUT, "/veiculos/**" ).hasRole( "ADMIN" )
                    .requestMatchers( HttpMethod.PATCH, "/veiculos/**" ).hasRole( "ADMIN" ).requestMatchers( HttpMethod.DELETE, "/veiculos/**" )
                    .hasRole( "ADMIN" ).anyRequest().authenticated() )
            .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) );

        return http.build();
    }
}