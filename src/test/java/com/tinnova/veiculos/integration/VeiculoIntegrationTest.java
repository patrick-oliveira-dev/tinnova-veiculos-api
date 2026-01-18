package com.tinnova.veiculos.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinnova.veiculos.dto.request.LoginRequest;
import com.tinnova.veiculos.dto.request.VeiculoRequest;
import com.tinnova.veiculos.dto.response.TokenResponse;
import com.tinnova.veiculos.entity.Usuario;
import com.tinnova.veiculos.entity.Veiculo;
import com.tinnova.veiculos.enums.Role;
import com.tinnova.veiculos.repository.UsuarioRepository;
import com.tinnova.veiculos.repository.VeiculoRepository;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles( "test" )
@DisplayName( "Testes de Integração - Fluxo Completo" )
class VeiculoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    private String userToken;

    @BeforeEach
    void setUp()
        throws Exception {

        // Limpar banco
        veiculoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Criar usuários
        Usuario admin = new Usuario();
        admin.setUsername( "admin" );
        admin.setPassword( passwordEncoder.encode( "admin123" ) );
        admin.setRole( Role.ADMIN );
        admin.setAtivo( true );
        usuarioRepository.save( admin );

        Usuario user = new Usuario();
        user.setUsername( "user" );
        user.setPassword( passwordEncoder.encode( "user123" ) );
        user.setRole( Role.USER );
        user.setAtivo( true );
        usuarioRepository.save( user );

        // Obter tokens
        adminToken = obterToken( "admin", "admin123" );
        userToken = obterToken( "user", "user123" );
    }


    private String obterToken( String username, String password )
        throws Exception {

        LoginRequest loginRequest = new LoginRequest( username, password );

        MvcResult result =
            mockMvc.perform( post( "/auth/login" ).contentType( MediaType.APPLICATION_JSON ).content( objectMapper.writeValueAsString( loginRequest ) ) )
                .andExpect( status().isOk() ).andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue( responseBody, TokenResponse.class );

        return tokenResponse.getToken();
    }


    @Test
    @DisplayName( "Fluxo completo: Login ADMIN → Criar Veículo → Listar → Buscar por ID → Atualizar → Deletar" )
    void fluxoCompletoAdmin()
        throws Exception {

        // 1. Criar veículo como ADMIN
        VeiculoRequest createRequest = new VeiculoRequest();
        createRequest.setMarca( "Toyota" );
        createRequest.setModelo( "Corolla" );
        createRequest.setAno( 2023 );
        createRequest.setCor( "Preto" );
        createRequest.setPlaca( "ABC1234" );
        createRequest.setPrecoBrl( new BigDecimal( "100000.00" ) );

        MvcResult createResult = mockMvc
            .perform(
                post( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                    .content( objectMapper.writeValueAsString( createRequest ) ) )
            .andExpect( status().isCreated() ).andExpect( jsonPath( "$.marca" ).value( "Toyota" ) ).andExpect( jsonPath( "$.placa" ).value( "ABC1234" ) )
            .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long veiculoId = objectMapper.readTree( createResponseBody ).get( "id" ).asLong();

        // 2. Listar veículos
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) );

        // 3. Buscar por ID
        mockMvc.perform( get( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + adminToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.id" ).value( veiculoId ) ).andExpect( jsonPath( "$.marca" ).value( "Toyota" ) );

        // 4. Atualizar veículo
        VeiculoRequest updateRequest = new VeiculoRequest();
        updateRequest.setMarca( "Toyota" );
        updateRequest.setModelo( "Corolla XEI" );
        updateRequest.setAno( 2024 );
        updateRequest.setCor( "Azul" );
        updateRequest.setPlaca( "ABC1234" );
        updateRequest.setPrecoBrl( new BigDecimal( "110000.00" ) );

        mockMvc
            .perform(
                put( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                    .content( objectMapper.writeValueAsString( updateRequest ) ) )
            .andExpect( status().isOk() ).andExpect( jsonPath( "$.modelo" ).value( "Corolla XEI" ) ).andExpect( jsonPath( "$.ano" ).value( 2024 ) );

        // 5. Deletar veículo (soft delete)
        mockMvc.perform( delete( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + adminToken ) ).andExpect( status().isNoContent() );

        // 6. Verificar que não aparece mais na listagem
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content" ).isEmpty() );

        // 7. Verificar que está inativo no banco
        Veiculo veiculoInativo = veiculoRepository.findById( veiculoId ).orElseThrow();
        assertThat( veiculoInativo.getAtivo() ).isFalse();
    }


    @Test
    @DisplayName( "Fluxo USER: Consegue listar e buscar, mas não criar/atualizar/deletar" )
    void fluxoCompletoUser()
        throws Exception {

        // Criar um veículo como ADMIN primeiro
        VeiculoRequest createRequest = new VeiculoRequest();
        createRequest.setMarca( "Honda" );
        createRequest.setModelo( "Civic" );
        createRequest.setAno( 2023 );
        createRequest.setCor( "Branco" );
        createRequest.setPlaca( "XYZ5678" );
        createRequest.setPrecoBrl( new BigDecimal( "120000.00" ) );

        MvcResult createResult = mockMvc.perform(
            post( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( createRequest ) ) )
            .andExpect( status().isCreated() ).andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long veiculoId = objectMapper.readTree( createResponseBody ).get( "id" ).asLong();

        // USER consegue listar
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + userToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content[0].marca" ).value( "Honda" ) );

        // USER consegue buscar por ID
        mockMvc.perform( get( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + userToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.marca" ).value( "Honda" ) );

        // USER NÃO consegue criar
        mockMvc.perform(
            post( "/veiculos" ).header( "Authorization", "Bearer " + userToken ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( createRequest ) ) )
            .andExpect( status().isForbidden() );

        // USER NÃO consegue atualizar
        mockMvc.perform(
            put( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + userToken ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( createRequest ) ) )
            .andExpect( status().isForbidden() );

        // USER NÃO consegue deletar
        mockMvc.perform( delete( "/veiculos/" + veiculoId ).header( "Authorization", "Bearer " + userToken ) ).andExpect( status().isForbidden() );
    }


    @Test
    @DisplayName( "Fluxo: Filtros combinados e relatórios" )
    void fluxoFiltrosERelatorios()
        throws Exception {

        // Criar múltiplos veículos
        criarVeiculo( "Toyota", "Corolla", 2023, "Preto", "AAA1111", "100000" );
        criarVeiculo( "Toyota", "Hilux", 2023, "Branco", "BBB2222", "150000" );
        criarVeiculo( "Honda", "Civic", 2023, "Azul", "CCC3333", "120000" );
        criarVeiculo( "Honda", "HR-V", 2022, "Prata", "DDD4444", "110000" );

        // Filtrar por marca
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + userToken ).param( "marca", "Toyota" ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content.length()" ).value( 2 ) ).andExpect( jsonPath( "$.content[0].marca" ).value( "Toyota" ) );

        // Filtrar por ano
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + userToken ).param( "ano", "2023" ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content.length()" ).value( 3 ) );

        // Filtrar por cor
        mockMvc.perform( get( "/veiculos" ).header( "Authorization", "Bearer " + userToken ).param( "cor", "Branco" ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content.length()" ).value( 1 ) );

        // Relatório por marca
        mockMvc.perform( get( "/veiculos/relatorios/por-marca" ).header( "Authorization", "Bearer " + userToken ) ).andExpect( status().isOk() )
            .andExpect( jsonPath( "$.length()" ).value( 2 ) ).andExpect( jsonPath( "$[0].quantidade" ).value( 2 ) );
    }


    @Test
    @DisplayName( "Fluxo: Validação de placa duplicada" )
    void fluxoValidacaoPlacaDuplicada()
        throws Exception {

        VeiculoRequest request = new VeiculoRequest();
        request.setMarca( "Toyota" );
        request.setModelo( "Corolla" );
        request.setAno( 2023 );
        request.setCor( "Preto" );
        request.setPlaca( "DUP1234" );
        request.setPrecoBrl( new BigDecimal( "100000.00" ) );

        // Criar primeiro veículo
        mockMvc.perform(
            post( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) )
            .andExpect( status().isCreated() );

        // Tentar criar com mesma placa
        mockMvc
            .perform(
                post( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                    .content( objectMapper.writeValueAsString( request ) ) )
            .andExpect( status().isConflict() ).andExpect( jsonPath( "$.status" ).value( 409 ) )
            .andExpect( jsonPath( "$.message" ).value( "Já existe um veículo cadastrado com a placa: DUP1234" ) );
    }


    @Test
    @DisplayName( "Fluxo: Sem autenticação deve retornar 401" )
    void fluxoSemAutenticacao()
        throws Exception {

        mockMvc.perform( get( "/veiculos" ) ).andExpect( status().isUnauthorized() );

        mockMvc.perform( post( "/veiculos" ).contentType( MediaType.APPLICATION_JSON ).content( "{}" ) ).andExpect( status().isUnauthorized() );
    }


    private void criarVeiculo( String marca, String modelo, Integer ano, String cor, String placa, String precoBrl )
        throws Exception {

        VeiculoRequest request = new VeiculoRequest();
        request.setMarca( marca );
        request.setModelo( modelo );
        request.setAno( ano );
        request.setCor( cor );
        request.setPlaca( placa );
        request.setPrecoBrl( new BigDecimal( precoBrl ) );

        mockMvc.perform(
            post( "/veiculos" ).header( "Authorization", "Bearer " + adminToken ).contentType( MediaType.APPLICATION_JSON )
                .content( objectMapper.writeValueAsString( request ) ) )
            .andExpect( status().isCreated() );
    }
}