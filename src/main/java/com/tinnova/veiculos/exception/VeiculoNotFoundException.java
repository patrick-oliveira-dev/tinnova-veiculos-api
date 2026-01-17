package com.tinnova.veiculos.exception;


public class VeiculoNotFoundException extends RuntimeException {

    public VeiculoNotFoundException( String message ) {

        super( message );
    }
}