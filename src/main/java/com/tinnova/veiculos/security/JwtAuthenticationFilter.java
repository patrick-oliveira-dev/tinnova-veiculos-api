package com.tinnova.veiculos.security;


import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tinnova.veiculos.service.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UsuarioService usuarioService;

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain )
        throws ServletException,
        IOException {

        final String authorizationHeader = request.getHeader( "Authorization" );

        String username = null;
        String jwt = null;

        if ( authorizationHeader != null && authorizationHeader.startsWith( "Bearer " ) ) {
            jwt = authorizationHeader.substring( 7 );
            try {
                username = jwtUtil.extractUsername( jwt );
            } catch ( Exception e ) {
                log.error( "Erro ao extrair username do token: {}", e.getMessage() );
            }
        }

        if ( username != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
            UserDetails userDetails = usuarioService.loadUserByUsername( username );

            if ( jwtUtil.validateToken( jwt, userDetails ) ) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities() );

                authToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
                SecurityContextHolder.getContext().setAuthentication( authToken );

                log.debug( "Usuário autenticado: {}", username );
            }
        }

        filterChain.doFilter( request, response );
    }
}