package com.tinnova.veiculos.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tinnova.veiculos.dto.request.LoginRequest;
import com.tinnova.veiculos.dto.response.TokenResponse;
import com.tinnova.veiculos.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UsuarioService usuarioService;

    private final JwtUtil jwtUtil;

    public TokenResponse login( LoginRequest request ) {

        log.info( "Tentativa de login do usuário: {}", request.getUsername() );

        authenticationManager.authenticate( new UsernamePasswordAuthenticationToken( request.getUsername(), request.getPassword() ) );

        UserDetails userDetails = usuarioService.loadUserByUsername( request.getUsername() );

        String token = jwtUtil.generateToken( userDetails );

        log.info( "Login realizado com sucesso para o usuário: {}", request.getUsername() );

        return TokenResponse.builder().token( token ).type( "Bearer" ).expiresIn( jwtUtil.getExpirationTime() ).build();
    }
}