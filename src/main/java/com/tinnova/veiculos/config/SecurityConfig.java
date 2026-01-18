package com.tinnova.veiculos.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tinnova.veiculos.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http )
        throws Exception {

        http.csrf( AbstractHttpConfigurer::disable ).sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
            .exceptionHandling( exception -> exception.authenticationEntryPoint( ( request, response, authException ) -> {
                response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                response.setContentType( "application/json" );

                String json = String.format(
                    "{\"status\": 401, \"message\": \"Acesso negado: Token ausente ou inválido\", \"timestamp\": \"%s\"}",
                    java.time.LocalDateTime.now() );
                response.getWriter().write( json );
            } ) )
            .authorizeHttpRequests(
                auth -> auth.requestMatchers( "/auth/**" ).permitAll().requestMatchers( "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html" ).permitAll()
                    .requestMatchers( "/actuator/**" ).permitAll()
                    // Regras de acesso por Role
                    .requestMatchers( HttpMethod.GET, "/veiculos/**" ).hasAnyRole( "USER", "ADMIN" ).requestMatchers( HttpMethod.POST, "/veiculos/**" )
                    .hasRole( "ADMIN" ).requestMatchers( HttpMethod.PUT, "/veiculos/**" ).hasRole( "ADMIN" ).requestMatchers( HttpMethod.PATCH, "/veiculos/**" )
                    .hasRole( "ADMIN" ).requestMatchers( HttpMethod.DELETE, "/veiculos/**" ).hasRole( "ADMIN" ).anyRequest().authenticated() )
            .authenticationProvider( authenticationProvider() ).addFilterBefore( jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService( userDetailsService );
        provider.setPasswordEncoder( passwordEncoder() );
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager( AuthenticationConfiguration config )
        throws Exception {

        return config.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}