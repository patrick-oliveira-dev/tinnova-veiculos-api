package com.tinnova.veiculos.controller;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tinnova.veiculos.dto.request.VeiculoRequest;
import com.tinnova.veiculos.dto.response.RelatorioMarcaResponse;
import com.tinnova.veiculos.dto.response.VeiculoResponse;
import com.tinnova.veiculos.service.VeiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping( "/veiculos" )
@RequiredArgsConstructor
@Tag( name = "Veículos", description = "Endpoints para gerenciamento de veículos" )
@SecurityRequirement( name = "bearerAuth" )
public class VeiculoController {

    private final VeiculoService veiculoService;

    @GetMapping
    @Operation( summary = "Listar todos os veículos", description = "Retorna todos os veículos com paginação e ordenação" )
    public ResponseEntity< Page< VeiculoResponse > > findAll(
        @PageableDefault( size = 10, sort = "id", direction = Sort.Direction.ASC ) Pageable pageable,
        @RequestParam( required = false ) String marca,
        @RequestParam( required = false ) Integer ano,
        @RequestParam( required = false ) String cor,
        @RequestParam( required = false ) BigDecimal minPreco,
        @RequestParam( required = false ) BigDecimal maxPreco ) {

        Page< VeiculoResponse > response;

        if ( minPreco != null || maxPreco != null ) {
            response = veiculoService.findByPrecoRange( minPreco, maxPreco, pageable );
        } else if ( marca != null || ano != null || cor != null ) {
            response = veiculoService.findByFiltros( marca, ano, cor, pageable );
        } else {
            response = veiculoService.findAll( pageable );
        }

        return ResponseEntity.ok( response );
    }


    @GetMapping( "/{id}" )
    @Operation( summary = "Buscar veículo por ID", description = "Retorna os detalhes de um veículo específico" )
    public ResponseEntity< VeiculoResponse > findById( @PathVariable Long id ) {

        VeiculoResponse response = veiculoService.findById( id );
        return ResponseEntity.ok( response );
    }


    @PostMapping
    @Operation( summary = "Criar novo veículo", description = "Cadastra um novo veículo (apenas ADMIN)" )
    public ResponseEntity< VeiculoResponse > create( @Valid @RequestBody VeiculoRequest request ) {

        VeiculoResponse response = veiculoService.create( request );
        return ResponseEntity.status( HttpStatus.CREATED ).body( response );
    }


    @PutMapping( "/{id}" )
    @Operation( summary = "Atualizar veículo", description = "Atualiza todos os dados de um veículo (apenas ADMIN)" )
    public ResponseEntity< VeiculoResponse > update( @PathVariable Long id, @Valid @RequestBody VeiculoRequest request ) {

        VeiculoResponse response = veiculoService.update( id, request );
        return ResponseEntity.ok( response );
    }


    @PatchMapping( "/{id}" )
    @Operation( summary = "Atualizar parcialmente veículo", description = "Atualiza parcialmente os dados de um veículo (apenas ADMIN)" )
    public ResponseEntity< VeiculoResponse > partialUpdate( @PathVariable Long id, @RequestBody VeiculoRequest request ) {

        VeiculoResponse response = veiculoService.partialUpdate( id, request );
        return ResponseEntity.ok( response );
    }


    @DeleteMapping( "/{id}" )
    @Operation( summary = "Remover veículo", description = "Remove um veículo (soft delete - apenas ADMIN)" )
    public ResponseEntity< Void > delete( @PathVariable Long id ) {

        veiculoService.delete( id );
        return ResponseEntity.noContent().build();
    }


    @GetMapping( "/relatorios/por-marca" )
    @Operation( summary = "Relatório por marca", description = "Retorna a quantidade de veículos agrupados por marca" )
    public ResponseEntity< List< RelatorioMarcaResponse > > getRelatorioPorMarca() {

        List< RelatorioMarcaResponse > response = veiculoService.getRelatorioPorMarca();
        return ResponseEntity.ok( response );
    }
}