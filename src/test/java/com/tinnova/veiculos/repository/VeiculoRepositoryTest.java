package com.tinnova.veiculos.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.tinnova.veiculos.dto.response.RelatorioMarcaResponse;
import com.tinnova.veiculos.entity.Veiculo;


@DataJpaTest
@ActiveProfiles( "test" )
@DisplayName( "Testes do VeiculoRepository" )
class VeiculoRepositoryTest {

    @Autowired
    private VeiculoRepository veiculoRepository;

    private Veiculo veiculo1;

    private Veiculo veiculo2;

    private Veiculo veiculo3;

    @BeforeEach
    void setUp() {

        veiculoRepository.deleteAll();

        veiculo1 = new Veiculo();
        veiculo1.setMarca( "Toyota" );
        veiculo1.setModelo( "Corolla" );
        veiculo1.setAno( 2023 );
        veiculo1.setCor( "Preto" );
        veiculo1.setPlaca( "ABC1234" );
        veiculo1.setPrecoUsd( new BigDecimal( "20000.00" ) );
        veiculo1.setAtivo( true );

        veiculo2 = new Veiculo();
        veiculo2.setMarca( "Honda" );
        veiculo2.setModelo( "Civic" );
        veiculo2.setAno( 2023 );
        veiculo2.setCor( "Branco" );
        veiculo2.setPlaca( "XYZ5678" );
        veiculo2.setPrecoUsd( new BigDecimal( "25000.00" ) );
        veiculo2.setAtivo( true );

        veiculo3 = new Veiculo();
        veiculo3.setMarca( "Toyota" );
        veiculo3.setModelo( "Hilux" );
        veiculo3.setAno( 2022 );
        veiculo3.setCor( "Prata" );
        veiculo3.setPlaca( "DEF9012" );
        veiculo3.setPrecoUsd( new BigDecimal( "30000.00" ) );
        veiculo3.setAtivo( false ); // Inativo

        veiculoRepository.save( veiculo1 );
        veiculoRepository.save( veiculo2 );
        veiculoRepository.save( veiculo3 );
    }


    @Test
    @DisplayName( "Deve retornar apenas veículos ativos" )
    void deveBuscarApenasVeiculosAtivos() {

        Pageable pageable = PageRequest.of( 0, 10 );

        Page< Veiculo > result = veiculoRepository.findByAtivoTrue( pageable );

        assertThat( result.getContent() ).hasSize( 2 );
        assertThat( result.getContent() ).allMatch( Veiculo::getAtivo );
    }


    @Test
    @DisplayName( "Deve encontrar veículo ativo por ID" )
    void deveEncontrarVeiculoAtivoPorId() {

        Optional< Veiculo > result = veiculoRepository.findByIdAndAtivoTrue( veiculo1.getId() );

        assertThat( result ).isPresent();
        assertThat( result.get().getPlaca() ).isEqualTo( "ABC1234" );
    }


    @Test
    @DisplayName( "Não deve encontrar veículo inativo por ID" )
    void naoDeveEncontrarVeiculoInativoPorId() {

        Optional< Veiculo > result = veiculoRepository.findByIdAndAtivoTrue( veiculo3.getId() );

        assertThat( result ).isEmpty();
    }


    @Test
    @DisplayName( "Deve validar se placa existe entre veículos ativos" )
    void deveValidarSePlacaExiste() {

        boolean existe = veiculoRepository.existsByPlacaAndAtivoTrue( "ABC1234" );
        boolean naoExiste = veiculoRepository.existsByPlacaAndAtivoTrue( "ZZZ9999" );

        assertThat( existe ).isTrue();
        assertThat( naoExiste ).isFalse();
    }


    @Test
    @DisplayName( "Deve validar placa duplicada excluindo o próprio ID" )
    void deveValidarPlacaDuplicadaExcluindoProprioId() {

        boolean duplicada = veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( "ABC1234", veiculo2.getId() );
        boolean naoDuplicada = veiculoRepository.existsByPlacaAndAtivoTrueAndIdNot( "ABC1234", veiculo1.getId() );

        assertThat( duplicada ).isTrue();
        assertThat( naoDuplicada ).isFalse();
    }


    @Test
    @DisplayName( "Deve filtrar veículos por marca" )
    void deveFiltrarPorMarca() {

        Pageable pageable = PageRequest.of( 0, 10 );

        Page< Veiculo > result = veiculoRepository.findByFiltros( "Toyota", null, null, pageable );

        assertThat( result.getContent() ).hasSize( 1 );
        assertThat( result.getContent().get( 0 ).getMarca() ).isEqualTo( "Toyota" );
    }


    @Test
    @DisplayName( "Deve filtrar veículos por ano" )
    void deveFiltrarPorAno() {

        Pageable pageable = PageRequest.of( 0, 10 );

        Page< Veiculo > result = veiculoRepository.findByFiltros( null, 2023, null, pageable );

        assertThat( result.getContent() ).hasSize( 2 );
        assertThat( result.getContent() ).allMatch( v -> v.getAno() == 2023 );
    }


    @Test
    @DisplayName( "Deve filtrar veículos por cor" )
    void deveFiltrarPorCor() {

        Pageable pageable = PageRequest.of( 0, 10 );

        Page< Veiculo > result = veiculoRepository.findByFiltros( null, null, "Preto", pageable );

        assertThat( result.getContent() ).hasSize( 1 );
        assertThat( result.getContent().get( 0 ).getCor() ).isEqualToIgnoringCase( "Preto" );
    }


    @Test
    @DisplayName( "Deve filtrar veículos com múltiplos filtros combinados" )
    void deveFiltrarComMultiplosFiltros() {

        Pageable pageable = PageRequest.of( 0, 10 );

        Page< Veiculo > result = veiculoRepository.findByFiltros( "Toyota", 2023, "Preto", pageable );

        assertThat( result.getContent() ).hasSize( 1 );
        assertThat( result.getContent().get( 0 ).getPlaca() ).isEqualTo( "ABC1234" );
    }


    @Test
    @DisplayName( "Deve filtrar veículos por range de preço" )
    void deveFiltrarPorRangeDePreco() {

        Pageable pageable = PageRequest.of( 0, 10 );
        BigDecimal minPreco = new BigDecimal( "20000" );
        BigDecimal maxPreco = new BigDecimal( "26000" );

        Page< Veiculo > result = veiculoRepository.findByPrecoRange( minPreco, maxPreco, pageable );

        assertThat( result.getContent() ).hasSize( 2 );
        assertThat( result.getContent() ).allMatch( v -> v.getPrecoUsd().compareTo( minPreco ) >= 0 && v.getPrecoUsd().compareTo( maxPreco ) <= 0 );
    }


    @Test
    @DisplayName( "Deve gerar relatório agrupado por marca" )
    void deveGerarRelatorioPorMarca() {

        List< RelatorioMarcaResponse > result = veiculoRepository.findRelatorioPorMarca();

        assertThat( result ).hasSize( 2 );

        RelatorioMarcaResponse toyota = result.stream().filter( r -> r.getMarca().equals( "Toyota" ) ).findFirst().orElseThrow();

        RelatorioMarcaResponse honda = result.stream().filter( r -> r.getMarca().equals( "Honda" ) ).findFirst().orElseThrow();

        assertThat( toyota.getQuantidade() ).isEqualTo( 1L ); // Apenas veículos ativos
        assertThat( honda.getQuantidade() ).isEqualTo( 1L );
    }


    @Test
    @DisplayName( "Deve respeitar constraint de placa única" )
    void deveRespeitarConstraintDePlacaUnica() {

        Veiculo veiculoDuplicado = new Veiculo();
        veiculoDuplicado.setMarca( "Ford" );
        veiculoDuplicado.setModelo( "Focus" );
        veiculoDuplicado.setAno( 2023 );
        veiculoDuplicado.setCor( "Azul" );
        veiculoDuplicado.setPlaca( "ABC1234" ); // Placa duplicada
        veiculoDuplicado.setPrecoUsd( new BigDecimal( "18000.00" ) );
        veiculoDuplicado.setAtivo( true );

        // Deve lançar exceção ao tentar salvar com placa duplicada
        assertThat( veiculoRepository.existsByPlacaAndAtivoTrue( "ABC1234" ) ).isTrue();
    }
}