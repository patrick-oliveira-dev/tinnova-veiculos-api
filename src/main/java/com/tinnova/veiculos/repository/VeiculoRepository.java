package com.tinnova.veiculos.repository;


import com.tinnova.veiculos.dto.response.RelatorioMarcaResponse;
import com.tinnova.veiculos.entity.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface VeiculoRepository extends JpaRepository< Veiculo, Long > {

    // Buscar apenas veículos ativos
    Page< Veiculo > findByAtivoTrue( Pageable pageable );


    Optional< Veiculo > findByIdAndAtivoTrue( Long id );


    boolean existsByPlacaAndAtivoTrue( String placa );


    boolean existsByPlacaAndAtivoTrueAndIdNot( String placa, Long id );


    // Filtros combinados
    @Query( "SELECT v FROM Veiculo v WHERE v.ativo = true " + "AND (:marca IS NULL OR LOWER(v.marca) = LOWER(:marca)) " + "AND (:ano IS NULL OR v.ano = :ano) "
        + "AND (:cor IS NULL OR LOWER(v.cor) = LOWER(:cor))" )
    Page< Veiculo > findByFiltros( @Param( "marca" ) String marca, @Param( "ano" ) Integer ano, @Param( "cor" ) String cor, Pageable pageable );


    // Filtro por range de preço
    @Query( "SELECT v FROM Veiculo v WHERE v.ativo = true " + "AND (:minPreco IS NULL OR v.precoUsd >= :minPreco) "
        + "AND (:maxPreco IS NULL OR v.precoUsd <= :maxPreco)" )
    Page< Veiculo > findByPrecoRange( @Param( "minPreco" ) BigDecimal minPreco, @Param( "maxPreco" ) BigDecimal maxPreco, Pageable pageable );


    // Relatório por marca
    @Query( "SELECT new com.tinnova.veiculos.dto.response.RelatorioMarcaResponse(v.marca, COUNT(v)) "
        + "FROM Veiculo v WHERE v.ativo = true GROUP BY v.marca ORDER BY COUNT(v) DESC" )
    List< RelatorioMarcaResponse > findRelatorioPorMarca();
}