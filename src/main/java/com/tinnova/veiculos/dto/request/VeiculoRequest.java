package com.tinnova.veiculos.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoRequest {

    @NotBlank( message = "Marca é obrigatória" )
    @Size( min = 2, max = 50, message = "Marca deve ter entre 2 e 50 caracteres" )
    private String marca;

    @NotBlank( message = "Modelo é obrigatório" )
    @Size( min = 2, max = 100, message = "Modelo deve ter entre 2 e 100 caracteres" )
    private String modelo;

    @NotNull( message = "Ano é obrigatório" )
    @Min( value = 1900, message = "Ano deve ser maior ou igual a 1900" )
    @Max( value = 2100, message = "Ano deve ser menor ou igual a 2100" )
    private Integer ano;

    @NotBlank( message = "Cor é obrigatória" )
    @Size( min = 3, max = 30, message = "Cor deve ter entre 3 e 30 caracteres" )
    private String cor;

    @NotBlank( message = "Placa é obrigatória" )
    @Pattern( regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "Placa deve estar no formato brasileiro (AAA0A00 ou AAA0000)" )
    private String placa;

    @NotNull( message = "Preço é obrigatório" )
    @DecimalMin( value = "0.01", message = "Preço deve ser maior que zero" )
    private BigDecimal precoBrl; // Recebe em BRL, converte para USD
}