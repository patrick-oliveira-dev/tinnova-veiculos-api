package com.tinnova.veiculos.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMarcaResponse {

    private String marca;

    private Long quantidade;
}