package com.tinnova.veiculos.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tinnova.veiculos.entity.Usuario;
import com.tinnova.veiculos.enums.Role;
import com.tinnova.veiculos.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run( String... args )
        throws Exception {

        if ( !usuarioRepository.existsByUsername( "admin" ) ) {
            Usuario admin = new Usuario();
            admin.setUsername( "admin" );
            admin.setPassword( passwordEncoder.encode( "admin123" ) );
            admin.setRole( Role.ADMIN );
            admin.setAtivo( true );

            usuarioRepository.save( admin );
            log.info( "Usuário ADMIN criado com sucesso - username: admin, password: admin123" );
        }

        if ( !usuarioRepository.existsByUsername( "user" ) ) {
            Usuario user = new Usuario();
            user.setUsername( "user" );
            user.setPassword( passwordEncoder.encode( "user123" ) );
            user.setRole( Role.USER );
            user.setAtivo( true );

            usuarioRepository.save( user );
            log.info( "Usuário USER criado com sucesso - username: user, password: user123" );
        }
    }
}