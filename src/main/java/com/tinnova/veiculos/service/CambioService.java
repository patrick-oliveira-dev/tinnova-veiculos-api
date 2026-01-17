package com.tinnova.veiculos.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;


@Service
@Slf4j
@RequiredArgsConstructor
public class CambioService {

    private final WebClient.Builder webClientBuilder;

    @Value( "${exchange.api.primary.url}" )
    private String primaryApiUrl;

    @Value( "${exchange.api.fallback.url}" )
    private String fallbackApiUrl;

    @Cacheable( value = "cotacao-dolar", unless = "#result == null" )
    public BigDecimal getCotacaoDolar() {

        log.info( "Buscando cotação do dólar..." );

        try {
            return getCotacaoAwesomeApi();
        } catch ( Exception e ) {
            log.warn( "Falha ao buscar cotação na API principal, tentando fallback...", e );
            return getCotacaoFrankfurter();
        }
    }


    private BigDecimal getCotacaoAwesomeApi() {

        try {
            WebClient webClient = webClientBuilder.build();
            String response = webClient.get().uri( primaryApiUrl ).retrieve().bodyToMono( String.class ).block();

            // Parse da resposta: {"USDBRL":{"bid":"5.1234"}}
            String bid = response.split( "\"bid\":\"" )[ 1 ].split( "\"" )[ 0 ];
            BigDecimal cotacao = new BigDecimal( bid );

            log.info( "Cotação obtida da AwesomeAPI: {}", cotacao );
            return cotacao;

        } catch ( Exception e ) {
            log.error( "Erro ao buscar cotação na AwesomeAPI", e );
            throw new RuntimeException( "Erro ao buscar cotação do dólar", e );
        }
    }


    private BigDecimal getCotacaoFrankfurter() {

        try {
            WebClient webClient = webClientBuilder.build();
            String response = webClient.get().uri( fallbackApiUrl ).retrieve().bodyToMono( String.class ).block();

            // Parse da resposta: {"rates":{"BRL":5.1234}}
            String rate = response.split( "\"BRL\":" )[ 1 ].split( "}" )[ 0 ];
            BigDecimal cotacao = new BigDecimal( rate );

            log.info( "Cotação obtida da Frankfurter: {}", cotacao );
            return cotacao;

        } catch ( Exception e ) {
            log.error( "Erro ao buscar cotação na Frankfurter", e );
            throw new RuntimeException( "Erro ao buscar cotação do dólar no fallback", e );
        }
    }


    public BigDecimal convertBrlToUsd( BigDecimal valorBrl ) {

        BigDecimal cotacao = getCotacaoDolar();
        return valorBrl.divide( cotacao, 2, BigDecimal.ROUND_HALF_UP );
    }


    public BigDecimal convertUsdToBrl( BigDecimal valorUsd ) {

        BigDecimal cotacao = getCotacaoDolar();
        return valorUsd.multiply( cotacao ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    }
}