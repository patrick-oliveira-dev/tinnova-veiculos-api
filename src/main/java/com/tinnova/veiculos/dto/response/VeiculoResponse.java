package com.tinnova.veiculos.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoResponse {

    private Long id;

    private String marca;

    private String modelo;

    private Integer ano;

    private String cor;

    private String placa;

    private BigDecimal precoUsd;

    private BigDecimal precoBrl; // Calculado na conversão

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}