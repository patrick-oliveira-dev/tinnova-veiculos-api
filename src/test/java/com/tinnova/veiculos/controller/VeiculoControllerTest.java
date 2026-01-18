package com.tinnova.veiculos.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinnova.veiculos.config.TestSecurityConfig;
import com.tinnova.veiculos.dto.request.VeiculoRequest;
import com.tinnova.veiculos.dto.response.VeiculoResponse;
import com.tinnova.veiculos.exception.DuplicatePlacaException;
import com.tinnova.veiculos.exception.VeiculoNotFoundException;
import com.tinnova.veiculos.service.VeiculoService;


@SpringBootTest
@AutoConfigureMockMvc
@Import( TestSecurityConfig.class )
@ActiveProfiles( "test" )
@DisplayName( "Testes do VeiculoController" )
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VeiculoService veiculoService;

    private VeiculoRequest veiculoRequest;

    private VeiculoResponse veiculoResponse;

    @BeforeEach
    void setUp() {

        veiculoRequest = new VeiculoRequest();
        veiculoRequest.setMarca( "Toyota" );
        veiculoRequest.setModelo( "Corolla" );
        veiculoRequest.setAno( 2023 );
        veiculoRequest.setCor( "Preto" );
        veiculoRequest.setPlaca( "ABC1234" );
        veiculoRequest.setPrecoBrl( new BigDecimal( "100000.00" ) );

        veiculoResponse = VeiculoResponse.builder().id( 1L ).marca( "Toyota" ).modelo( "Corolla" ).ano( 2023 ).cor( "Preto" ).placa( "ABC1234" )
            .precoUsd( new BigDecimal( "20000.00" ) ).precoBrl( new BigDecimal( "100000.00" ) ).build();
    }


    @Test
    @DisplayName( "GET /veiculos - Deve retornar 403 quando não autenticado" )
    void deveRetornar403QuandoNaoAutenticado()
        throws Exception {

        mockMvc.perform( get( "/veiculos" ) ).andExpect( status().isForbidden() ); // 403 em vez de 401
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos - USER deve conseguir listar veículos" )
    void userDeveConseguirListarVeiculos()
        throws Exception {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< VeiculoResponse > page = new PageImpl<>( List.of( veiculoResponse ), pageable, 1 );

        when( veiculoService.findAll( any( Pageable.class ) ) ).thenReturn( page );

        mockMvc.perform( get( "/veiculos" ) ).andExpect( status().isOk() ).andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) )
            .andExpect( jsonPath( "$.content[0].placa" ).value( "ABC1234" ) );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "GET /veiculos - ADMIN deve conseguir listar veículos" )
    void adminDeveConseguirListarVeiculos()
        throws Exception {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< VeiculoResponse > page = new PageImpl<>( List.of( veiculoResponse ), pageable, 1 );
        when( veiculoService.findAll( any( Pageable.class ) ) ).thenReturn( page );

        mockMvc.perform( get( "/veiculos" ) ).andExpect( status().isOk() ).andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos/{id} - USER deve conseguir buscar por ID" )
    void userDeveConseguirBuscarPorId()
        throws Exception {

        when( veiculoService.findById( 1L ) ).thenReturn( veiculoResponse );

        mockMvc.perform( get( "/veiculos/1" ) ).andExpect( status().isOk() ).andExpect( jsonPath( "$.id" ).value( 1 ) )
            .andExpect( jsonPath( "$.marca" ).value( "Toyota" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos/{id} - Deve retornar 404 quando veículo não existe" )
    void deveRetornar404QuandoVeiculoNaoExiste()
        throws Exception {

        when( veiculoService.findById( 999L ) ).thenThrow( new VeiculoNotFoundException( "Veículo não encontrado com ID: 999" ) );

        mockMvc.perform( get( "/veiculos/999" ) ).andExpect( status().isNotFound() ).andExpect( jsonPath( "$.status" ).value( 404 ) )
            .andExpect( jsonPath( "$.message" ).value( "Veículo não encontrado com ID: 999" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "POST /veiculos - USER deve receber 403 (Forbidden)" )
    void userNaoDeveCriarVeiculo()
        throws Exception {

        mockMvc.perform( post( "/veiculos" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isForbidden() );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "POST /veiculos - ADMIN deve conseguir criar veículo" )
    void adminDeveConseguirCriarVeiculo()
        throws Exception {

        when( veiculoService.create( any( VeiculoRequest.class ) ) ).thenReturn( veiculoResponse );

        mockMvc.perform( post( "/veiculos" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isCreated() ).andExpect( jsonPath( "$.marca" ).value( "Toyota" ) ).andExpect( jsonPath( "$.placa" ).value( "ABC1234" ) );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "POST /veiculos - Deve retornar 409 quando placa duplicada" )
    void deveRetornar409QuandoPlacaDuplicada()
        throws Exception {

        when( veiculoService.create( any( VeiculoRequest.class ) ) )
            .thenThrow( new DuplicatePlacaException( "Já existe um veículo cadastrado com a placa: ABC1234" ) );

        mockMvc.perform( post( "/veiculos" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isConflict() ).andExpect( jsonPath( "$.status" ).value( 409 ) )
            .andExpect( jsonPath( "$.message" ).value( "Já existe um veículo cadastrado com a placa: ABC1234" ) );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "POST /veiculos - Deve retornar 400 quando dados inválidos" )
    void deveRetornar400QuandoDadosInvalidos()
        throws Exception {

        VeiculoRequest requestInvalido = new VeiculoRequest();
        requestInvalido.setMarca( "" ); // Marca vazia
        requestInvalido.setPlaca( "INVALIDA" ); // Placa inválida

        mockMvc.perform( post( "/veiculos" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( requestInvalido ) ) )
            .andExpect( status().isBadRequest() ).andExpect( jsonPath( "$.status" ).value( 400 ) ).andExpect( jsonPath( "$.details" ).isArray() );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "PUT /veiculos/{id} - USER deve receber 403" )
    void userNaoDeveAtualizarVeiculo()
        throws Exception {

        mockMvc.perform( put( "/veiculos/1" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isForbidden() );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "PUT /veiculos/{id} - ADMIN deve conseguir atualizar" )
    void adminDeveConseguirAtualizarVeiculo()
        throws Exception {

        when( veiculoService.update( eq( 1L ), any( VeiculoRequest.class ) ) ).thenReturn( veiculoResponse );

        mockMvc.perform( put( "/veiculos/1" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isOk() ).andExpect( jsonPath( "$.marca" ).value( "Toyota" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "PATCH /veiculos/{id} - USER deve receber 403" )
    void userNaoDeveAtualizarParcialmenteVeiculo()
        throws Exception {

        mockMvc.perform( patch( "/veiculos/1" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isForbidden() );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "PATCH /veiculos/{id} - ADMIN deve conseguir atualizar parcialmente" )
    void adminDeveConseguirAtualizarParcialmente()
        throws Exception {

        when( veiculoService.partialUpdate( eq( 1L ), any( VeiculoRequest.class ) ) ).thenReturn( veiculoResponse );

        mockMvc.perform( patch( "/veiculos/1" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( veiculoRequest ) ) )
            .andExpect( status().isOk() ).andExpect( jsonPath( "$.marca" ).value( "Toyota" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "DELETE /veiculos/{id} - USER deve receber 403" )
    void userNaoDeveDeletarVeiculo()
        throws Exception {

        mockMvc.perform( delete( "/veiculos/1" ) ).andExpect( status().isForbidden() );
    }


    @Test
    @WithMockUser( roles = "ADMIN" )
    @DisplayName( "DELETE /veiculos/{id} - ADMIN deve conseguir deletar" )
    void adminDeveConseguirDeletarVeiculo()
        throws Exception {

        doNothing().when( veiculoService ).delete( 1L );

        mockMvc.perform( delete( "/veiculos/1" ) ).andExpect( status().isNoContent() );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos/relatorios/por-marca - USER deve conseguir acessar" )
    void userDeveConseguirAcessarRelatorio()
        throws Exception {

        when( veiculoService.getRelatorioPorMarca() ).thenReturn( List.of() );

        mockMvc.perform( get( "/veiculos/relatorios/por-marca" ) ).andExpect( status().isOk() );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos - Deve filtrar por marca, ano e cor" )
    void deveFiltrarPorParametros()
        throws Exception {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< VeiculoResponse > page = new PageImpl<>( List.of( veiculoResponse ), pageable, 1 );
        when( veiculoService.findByFiltros( eq( "Toyota" ), eq( 2023 ), eq( "Preto" ), any( Pageable.class ) ) ).thenReturn( page );

        mockMvc.perform( get( "/veiculos" ).param( "marca", "Toyota" ).param( "ano", "2023" ).param( "cor", "Preto" ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) );
    }


    @Test
    @WithMockUser( roles = "USER" )
    @DisplayName( "GET /veiculos - Deve filtrar por range de preço" )
    void deveFiltrarPorRangeDePreco()
        throws Exception {

        Pageable pageable = PageRequest.of( 0, 10 );
        Page< VeiculoResponse > page = new PageImpl<>( List.of( veiculoResponse ), pageable, 1 );
        when( veiculoService.findByPrecoRange( any(), any(), any( Pageable.class ) ) ).thenReturn( page );

        mockMvc.perform( get( "/veiculos" ).param( "minPreco", "50000" ).param( "maxPreco", "150000" ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) );
    }
}