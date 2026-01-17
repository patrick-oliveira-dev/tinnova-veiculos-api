package com.tinnova.veiculos.entity;


import com.tinnova.veiculos.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Entity
@Table( name = "usuarios" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( nullable = false, unique = true )
    private String username;

    @Column( nullable = false )
    private String password;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false )
    private Role role;

    @Column( nullable = false )
    private Boolean ativo = true;

    @Override
    public Collection< ? extends GrantedAuthority > getAuthorities() {

        return List.of( new SimpleGrantedAuthority( "ROLE_" + role.name() ) );
    }


    @Override
    public boolean isAccountNonExpired() {

        return true;
    }


    @Override
    public boolean isAccountNonLocked() {

        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }


    @Override
    public boolean isEnabled() {

        return ativo;
    }
}