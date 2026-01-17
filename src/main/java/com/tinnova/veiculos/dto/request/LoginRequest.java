package com.tinnova.veiculos.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank( message = "Username é obrigatório" )
    private String username;

    @NotBlank( message = "Password é obrigatório" )
    private String password;
}