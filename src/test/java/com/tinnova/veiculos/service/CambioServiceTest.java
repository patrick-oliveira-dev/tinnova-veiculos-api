package com.tinnova.veiculos.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;


@ExtendWith( MockitoExtension.class )
@DisplayName( "Testes do CambioService" )
class CambioServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CambioService cambioService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField( cambioService, "primaryApiUrl", "https://economia.awesomeapi.com.br/json/last/USD-BRL" );
        ReflectionTestUtils.setField( cambioService, "fallbackApiUrl", "https://api.frankfurter.app/latest?from=USD&to=BRL" );
    }


    @Test
    @DisplayName( "Deve buscar cotação do dólar com sucesso da API principal" )
    void deveBuscarCotacaoComSucesso() {

        String mockResponse = "{\"USDBRL\":{\"bid\":\"5.25\"}}";

        when( webClientBuilder.build() ).thenReturn( webClient );
        when( webClient.get() ).thenReturn( requestHeadersUriSpec );
        when( requestHeadersUriSpec.uri( anyString() ) ).thenReturn( requestHeadersSpec );
        when( requestHeadersSpec.retrieve() ).thenReturn( responseSpec );
        when( responseSpec.bodyToMono( String.class ) ).thenReturn( Mono.just( mockResponse ) );

        BigDecimal cotacao = cambioService.getCotacaoDolar();

        assertThat( cotacao ).isEqualByComparingTo( new BigDecimal( "5.25" ) );
        verify( webClientBuilder, times( 1 ) ).build();
    }


    @Test
    @DisplayName( "Deve usar API de fallback quando a principal falhar" )
    void deveUsarFallbackQuandoPrincipalFalhar() {

        String mockFallbackResponse = "{\"rates\":{\"BRL\":5.30}}";

        // Primeira chamada falha
        when( webClientBuilder.build() ).thenReturn( webClient );
        when( webClient.get() ).thenReturn( requestHeadersUriSpec );
        when( requestHeadersUriSpec.uri( anyString() ) ).thenReturn( requestHeadersSpec );
        when( requestHeadersSpec.retrieve() ).thenReturn( responseSpec );
        when( responseSpec.bodyToMono( String.class ) ).thenThrow( new RuntimeException( "API principal indisponível" ) )
            .thenReturn( Mono.just( mockFallbackResponse ) );

        BigDecimal cotacao = cambioService.getCotacaoDolar();

        assertThat( cotacao ).isEqualByComparingTo( new BigDecimal( "5.30" ) );
    }


    @Test
    @DisplayName( "Deve converter BRL para USD corretamente" )
    void deveConverterBrlParaUsd() {

        String mockResponse = "{\"USDBRL\":{\"bid\":\"5.00\"}}";

        when( webClientBuilder.build() ).thenReturn( webClient );
        when( webClient.get() ).thenReturn( requestHeadersUriSpec );
        when( requestHeadersUriSpec.uri( anyString() ) ).thenReturn( requestHeadersSpec );
        when( requestHeadersSpec.retrieve() ).thenReturn( responseSpec );
        when( responseSpec.bodyToMono( String.class ) ).thenReturn( Mono.just( mockResponse ) );

        BigDecimal valorBrl = new BigDecimal( "100.00" );
        BigDecimal valorUsd = cambioService.convertBrlToUsd( valorBrl );

        assertThat( valorUsd ).isEqualByComparingTo( new BigDecimal( "20.00" ) );
    }


    @Test
    @DisplayName( "Deve converter USD para BRL corretamente" )
    void deveConverterUsdParaBrl() {

        String mockResponse = "{\"USDBRL\":{\"bid\":\"5.00\"}}";

        when( webClientBuilder.build() ).thenReturn( webClient );
        when( webClient.get() ).thenReturn( requestHeadersUriSpec );
        when( requestHeadersUriSpec.uri( anyString() ) ).thenReturn( requestHeadersSpec );
        when( requestHeadersSpec.retrieve() ).thenReturn( responseSpec );
        when( responseSpec.bodyToMono( String.class ) ).thenReturn( Mono.just( mockResponse ) );

        BigDecimal valorUsd = new BigDecimal( "20.00" );
        BigDecimal valorBrl = cambioService.convertUsdToBrl( valorUsd );

        assertThat( valorBrl ).isEqualByComparingTo( new BigDecimal( "100.00" ) );
    }
}