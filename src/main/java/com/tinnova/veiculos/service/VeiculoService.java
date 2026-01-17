package com.tinnova.veiculos.service;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinnova.veiculos.dto.request.VeiculoRequest;
import com.tinnova.veiculos.dto.response.RelatorioMarcaResponse;
import com.tinnova.veiculos.dto.response.VeiculoResponse;
import com.tinnova.veiculos.entity.Veiculo;
import com.tinnova.veiculos.exception.DuplicatePlacaException;
import com.tinnova.veiculos.exception.VeiculoNotFoundException;
import com.tinnova.veiculos.repository.VeiculoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    private final CambioService cambioService;

    @Transactional( readOnly = true )
    public Page< VeiculoResponse > findAll( Pageable pageable ) {

        log.info( "Buscando todos os veículos ativos - página: {}", pageable.getPageNumber() );
        return veiculoRepository.findByAtivoTrue( pageable ).map( this::toResponse );
    }


    @Transactional( readOnly = true )
    public Page< VeiculoResponse > findByFiltros( String marca, Integer ano, String cor, Pageable pageable ) {

        log.info( "Buscando veículos com filtros - marca: {}, ano: {}, cor: {}", marca, ano, cor );
        return veiculoRepository.findByFiltros( marca, ano, cor, pageable ).map( this::toResponse );
    }


    @Transactional( readOnly = true )
    public Page< VeiculoResponse > findByPrecoRange( BigDecimal minPreco, BigDecimal maxPreco, Pageable pageable ) {

        log.info( "Buscando veículos por range de preço - min: {}, max: {}", minPreco, maxPreco );

        // Converter BRL para USD se necessário
        BigDecimal minPrecoUsd = minPreco != null ? cambioService.convertBrlToUsd( minPreco ) : null;
        BigDecimal maxPrecoUsd = maxPreco != null ? cambioService.convertBrlToUsd( maxPreco ) : null;

        return veiculoRepository.findByPrecoRange( minPrecoUsd, maxPrecoUsd, pageable ).map( this::toResponse );
    }


    @Transactional( readOnly = true )
    public VeiculoResponse findById( Long id ) {

        log.info( "Buscando veículo por ID: {}", id );
        Veiculo veiculo =
            veiculoRepository.findByIdAndAtivoTrue( id ).orElseThrow( () -> new VeiculoNotFoundException( "Veículo não encontrado com ID: " + id ) );
        return toResponse( veiculo );
    }


    @Transactional
    public VeiculoResponse create( VeiculoRequest request ) {

        log.info( "Criando novo veículo - placa: {}", request.getPlaca() );

        // Validar duplicidade de placa
        if ( veiculoRepository.existsByPlacaAndAtivoTrue( request.getPlaca() ) ) {
            throw new DuplicatePlacaException( "Já existe um veículo cadastrado com a placa: " + request.getPlaca() );
        }

        Veiculo veiculo = toEntity( request );
        Veiculo saved = veiculoRepository.save( veiculo );

        log.info( "Veículo criado com sucesso - ID: {}", saved.getId() );
        return toResponse( saved );
    }


    @Transactional
    public VeiculoResponse update( Long id, VeiculoRequest request ) {

        log.info( "Atualizando veículo - ID: {}", id );

        Veiculo veiculo =
            veiculoRepository.findByIdAndAtivoTrue( id ).orElseThrow( () -> new VeiculoNotFoundException( "Veículo não encontrado com ID: " + id ) );

        // Validar duplicidade de placa (exceto a placa atual do veículo)
        if ( veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( request.getPlaca(), id ) ) {
            throw new DuplicatePlacaException( "Já existe um veículo cadastrado com a placa: " + request.getPlaca() );
        }

        updateEntity( veiculo, request );
        Veiculo updated = veiculoRepository.save( veiculo );

        log.info( "Veículo atualizado com sucesso - ID: {}", id );
        return toResponse( updated );
    }


    @Transactional
    public VeiculoResponse partialUpdate( Long id, VeiculoRequest request ) {

        log.info( "Atualizando parcialmente veículo - ID: {}", id );

        Veiculo veiculo =
            veiculoRepository.findByIdAndAtivoTrue( id ).orElseThrow( () -> new VeiculoNotFoundException( "Veículo não encontrado com ID: " + id ) );

        // Atualizar apenas os campos não nulos
        if ( request.getMarca() != null ) {
            veiculo.setMarca( request.getMarca() );
        }
        if ( request.getModelo() != null ) {
            veiculo.setModelo( request.getModelo() );
        }
        if ( request.getAno() != null ) {
            veiculo.setAno( request.getAno() );
        }
        if ( request.getCor() != null ) {
            veiculo.setCor( request.getCor() );
        }
        if ( request.getPlaca() != null ) {
            if ( veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( request.getPlaca(), id ) ) {
                throw new DuplicatePlacaException( "Já existe um veículo cadastrado com a placa: " + request.getPlaca() );
            }
            veiculo.setPlaca( request.getPlaca() );
        }
        if ( request.getPrecoBrl() != null ) {
            BigDecimal precoUsd = cambioService.convertBrlToUsd( request.getPrecoBrl() );
            veiculo.setPrecoUsd( precoUsd );
        }

        Veiculo updated = veiculoRepository.save( veiculo );

        log.info( "Veículo atualizado parcialmente com sucesso - ID: {}", id );
        return toResponse( updated );
    }


    @Transactional
    public void delete( Long id ) {

        log.info( "Removendo veículo (soft delete) - ID: {}", id );

        Veiculo veiculo =
            veiculoRepository.findByIdAndAtivoTrue( id ).orElseThrow( () -> new VeiculoNotFoundException( "Veículo não encontrado com ID: " + id ) );

        veiculo.setAtivo( false );
        veiculoRepository.save( veiculo );

        log.info( "Veículo removido com sucesso - ID: {}", id );
    }


    @Transactional( readOnly = true )
    public List< RelatorioMarcaResponse > getRelatorioPorMarca() {

        log.info( "Gerando relatório de veículos por marca" );
        return veiculoRepository.findRelatorioPorMarca();
    }

    // Métodos auxiliares de conversão


    private VeiculoResponse toResponse( Veiculo veiculo ) {

        BigDecimal precoBrl = cambioService.convertUsdToBrl( veiculo.getPrecoUsd() );

        return VeiculoResponse.builder().id( veiculo.getId() ).marca( veiculo.getMarca() ).modelo( veiculo.getModelo() ).ano( veiculo.getAno() )
            .cor( veiculo.getCor() ).placa( veiculo.getPlaca() ).precoUsd( veiculo.getPrecoUsd() ).precoBrl( precoBrl ).createdAt( veiculo.getCreatedAt() )
            .updatedAt( veiculo.getUpdatedAt() ).build();
    }


    private Veiculo toEntity( VeiculoRequest request ) {

        BigDecimal precoUsd = cambioService.convertBrlToUsd( request.getPrecoBrl() );

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca( request.getMarca() );
        veiculo.setModelo( request.getModelo() );
        veiculo.setAno( request.getAno() );
        veiculo.setCor( request.getCor() );
        veiculo.setPlaca( request.getPlaca().toUpperCase() );
        veiculo.setPrecoUsd( precoUsd );
        veiculo.setAtivo( true );

        return veiculo;
    }


    private void updateEntity( Veiculo veiculo, VeiculoRequest request ) {

        BigDecimal precoUsd = cambioService.convertBrlToUsd( request.getPrecoBrl() );

        veiculo.setMarca( request.getMarca() );
        veiculo.setModelo( request.getModelo() );
        veiculo.setAno( request.getAno() );
        veiculo.setCor( request.getCor() );
        veiculo.setPlaca( request.getPlaca().toUpperCase() );
        veiculo.setPrecoUsd( precoUsd );
    }
}