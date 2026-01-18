package com.tinnova.veiculos.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tinnova.veiculos.dto.request.VeiculoRequest;
import com.tinnova.veiculos.dto.response.VeiculoResponse;
import com.tinnova.veiculos.entity.Veiculo;
import com.tinnova.veiculos.exception.DuplicatePlacaException;
import com.tinnova.veiculos.exception.VeiculoNotFoundException;
import com.tinnova.veiculos.repository.VeiculoRepository;


@ExtendWith( MockitoExtension.class )
@DisplayName( "Testes do VeiculoService" )
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private CambioService cambioService;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;

    private VeiculoRequest veiculoRequest;

    @BeforeEach
    void setUp() {

        veiculo = new Veiculo();
        veiculo.setId( 1L );
        veiculo.setMarca( "Toyota" );
        veiculo.setModelo( "Corolla" );
        veiculo.setAno( 2023 );
        veiculo.setCor( "Preto" );
        veiculo.setPlaca( "ABC1234" );
        veiculo.setPrecoUsd( new BigDecimal( "20000.00" ) );
        veiculo.setAtivo( true );

        veiculoRequest = new VeiculoRequest();
        veiculoRequest.setMarca( "Toyota" );
        veiculoRequest.setModelo( "Corolla" );
        veiculoRequest.setAno( 2023 );
        veiculoRequest.setCor( "Preto" );
        veiculoRequest.setPlaca( "ABC1234" );
        veiculoRequest.setPrecoBrl( new BigDecimal( "100000.00" ) );
    }


    @Test
    @DisplayName( "Deve buscar todos os veículos com sucesso" )
    void deveBuscarTodosVeiculos() {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< Veiculo > page = new PageImpl<>( List.of( veiculo ) );

        when( veiculoRepository.findByAtivoTrue( pageable ) ).thenReturn( page );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );

        Page< VeiculoResponse > result = veiculoService.findAll( pageable );

        assertThat( result.getContent() ).hasSize( 1 );
        assertThat( result.getContent().get( 0 ).getMarca() ).isEqualTo( "Toyota" );
        verify( veiculoRepository, times( 1 ) ).findByAtivoTrue( pageable );
    }


    @Test
    @DisplayName( "Deve buscar veículo por ID com sucesso" )
    void deveBuscarVeiculoPorId() {

        when( veiculoRepository.findByIdAndAtivoTrue( 1L ) ).thenReturn( Optional.of( veiculo ) );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );

        VeiculoResponse result = veiculoService.findById( 1L );

        assertThat( result ).isNotNull();
        assertThat( result.getId() ).isEqualTo( 1L );
        assertThat( result.getPlaca() ).isEqualTo( "ABC1234" );
    }


    @Test
    @DisplayName( "Deve lançar exceção quando veículo não for encontrado" )
    void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {

        when( veiculoRepository.findByIdAndAtivoTrue( 999L ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> veiculoService.findById( 999L ) ).isInstanceOf( VeiculoNotFoundException.class )
            .hasMessageContaining( "Veículo não encontrado com ID: 999" );
    }


    @Test
    @DisplayName( "Deve criar veículo com sucesso" )
    void deveCriarVeiculoComSucesso() {

        when( veiculoRepository.existsByPlacaAndAtivoTrue( anyString() ) ).thenReturn( false );
        when( cambioService.convertBrlToUsd( any() ) ).thenReturn( new BigDecimal( "20000.00" ) );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );
        when( veiculoRepository.save( any( Veiculo.class ) ) ).thenReturn( veiculo );

        VeiculoResponse result = veiculoService.create( veiculoRequest );

        assertThat( result ).isNotNull();
        assertThat( result.getMarca() ).isEqualTo( "Toyota" );
        verify( veiculoRepository, times( 1 ) ).save( any( Veiculo.class ) );
    }


    @Test
    @DisplayName( "Deve validar duplicidade de placa ao criar" )
    void deveValidarDuplicidadeDePlacaAoCriar() {

        when( veiculoRepository.existsByPlacaAndAtivoTrue( "ABC1234" ) ).thenReturn( true );

        assertThatThrownBy( () -> veiculoService.create( veiculoRequest ) ).isInstanceOf( DuplicatePlacaException.class )
            .hasMessageContaining( "Já existe um veículo cadastrado com a placa: ABC1234" );

        verify( veiculoRepository, never() ).save( any() );
    }


    @Test
    @DisplayName( "Deve atualizar veículo com sucesso" )
    void deveAtualizarVeiculoComSucesso() {

        when( veiculoRepository.findByIdAndAtivoTrue( 1L ) ).thenReturn( Optional.of( veiculo ) );
        when( veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( anyString(), anyLong() ) ).thenReturn( false );
        when( cambioService.convertBrlToUsd( any() ) ).thenReturn( new BigDecimal( "20000.00" ) );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );
        when( veiculoRepository.save( any( Veiculo.class ) ) ).thenReturn( veiculo );

        VeiculoResponse result = veiculoService.update( 1L, veiculoRequest );

        assertThat( result ).isNotNull();
        verify( veiculoRepository, times( 1 ) ).save( any( Veiculo.class ) );
    }


    @Test
    @DisplayName( "Deve validar duplicidade de placa ao atualizar" )
    void deveValidarDuplicidadeDePlacaAoAtualizar() {

        when( veiculoRepository.findByIdAndAtivoTrue( 1L ) ).thenReturn( Optional.of( veiculo ) );
        when( veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( "ABC1234", 1L ) ).thenReturn( true );

        assertThatThrownBy( () -> veiculoService.update( 1L, veiculoRequest ) ).isInstanceOf( DuplicatePlacaException.class );
    }


    @Test
    @DisplayName( "Deve atualizar parcialmente veículo com sucesso" )
    void deveAtualizarParcialmenteVeiculo() {

        VeiculoRequest requestParcial = new VeiculoRequest();
        requestParcial.setCor( "Azul" );

        when( veiculoRepository.findByIdAndAtivoTrue( 1L ) ).thenReturn( Optional.of( veiculo ) );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );
        when( veiculoRepository.save( any( Veiculo.class ) ) ).thenReturn( veiculo );

        VeiculoResponse result = veiculoService.partialUpdate( 1L, requestParcial );

        assertThat( result ).isNotNull();
        verify( veiculoRepository, times( 1 ) ).save( any( Veiculo.class ) );
    }


    @Test
    @DisplayName( "Deve realizar soft delete com sucesso" )
    void deveRealizarSoftDelete() {

        when( veiculoRepository.findByIdAndAtivoTrue( 1L ) ).thenReturn( Optional.of( veiculo ) );
        when( veiculoRepository.save( any( Veiculo.class ) ) ).thenReturn( veiculo );

        veiculoService.delete( 1L );

        verify( veiculoRepository, times( 1 ) ).save( any( Veiculo.class ) );
    }


    @Test
    @DisplayName( "Deve filtrar veículos por marca, ano e cor" )
    void deveFiltrarVeiculos() {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< Veiculo > page = new PageImpl<>( List.of( veiculo ) );

        when( veiculoRepository.findByFiltros( "Toyota", 2023, "Preto", pageable ) ).thenReturn( page );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );

        Page< VeiculoResponse > result = veiculoService.findByFiltros( "Toyota", 2023, "Preto", pageable );

        assertThat( result.getContent() ).hasSize( 1 );
        verify( veiculoRepository, times( 1 ) ).findByFiltros( "Toyota", 2023, "Preto", pageable );
    }


    @Test
    @DisplayName( "Deve filtrar veículos por range de preço" )
    void deveFiltrarPorRangeDePreco() {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< Veiculo > page = new PageImpl<>( List.of( veiculo ) );

        when( cambioService.convertBrlToUsd( any() ) ).thenReturn( new BigDecimal( "20000.00" ) );
        when( veiculoRepository.findByPrecoRange( any(), any(), eq( pageable ) ) ).thenReturn( page );
        when( cambioService.convertUsdToBrl( any() ) ).thenReturn( new BigDecimal( "100000.00" ) );

        Page< VeiculoResponse > result = veiculoService.findByPrecoRange( new BigDecimal( "50000" ), new BigDecimal( "150000" ), pageable );

        assertThat( result.getContent() ).hasSize( 1 );
    }
}