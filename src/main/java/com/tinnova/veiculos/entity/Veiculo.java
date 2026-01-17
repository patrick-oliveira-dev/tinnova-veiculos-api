package com.tinnova.veiculos.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table( name = "veiculos" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( nullable = false )
    private String marca;

    @Column( nullable = false )
    private String modelo;

    @Column( nullable = false )
    private Integer ano;

    @Column( nullable = false )
    private String cor;

    @Column( nullable = false, unique = true, length = 7 )
    private String placa;

    @Column( nullable = false, precision = 10, scale = 2 )
    private BigDecimal precoUsd; // Preço em dólar

    @Column( nullable = false )
    private Boolean ativo = true; // Soft delete

    @CreationTimestamp
    @Column( nullable = false, updatable = false )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column( nullable = false )
    private LocalDateTime updatedAt;
}