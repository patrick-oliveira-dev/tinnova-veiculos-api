package com.tinnova.veiculos.exception;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tinnova.veiculos.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler( VeiculoNotFoundException.class )
    public ResponseEntity< ErrorResponse > handleVeiculoNotFound( VeiculoNotFoundException ex, HttpServletRequest request ) {

        log.error( "Veículo não encontrado: {}", ex.getMessage() );

        ErrorResponse error = ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.NOT_FOUND.value() )
            .error( HttpStatus.NOT_FOUND.getReasonPhrase() ).message( ex.getMessage() ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( error );
    }


    @ExceptionHandler( DuplicatePlacaException.class )
    public ResponseEntity< ErrorResponse > handleDuplicatePlaca( DuplicatePlacaException ex, HttpServletRequest request ) {

        log.error( "Placa duplicada: {}", ex.getMessage() );

        ErrorResponse error = ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.CONFLICT.value() )
            .error( HttpStatus.CONFLICT.getReasonPhrase() ).message( ex.getMessage() ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.CONFLICT ).body( error );
    }


    @ExceptionHandler( MethodArgumentNotValidException.class )
    public ResponseEntity< ErrorResponse > handleValidationErrors( MethodArgumentNotValidException ex, HttpServletRequest request ) {

        log.error( "Erro de validação: {}", ex.getMessage() );

        List< String > details = new ArrayList<>();
        for ( FieldError error : ex.getBindingResult().getFieldErrors() ) {
            details.add( error.getField() + ": " + error.getDefaultMessage() );
        }

        ErrorResponse error =
            ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.BAD_REQUEST.value() ).error( HttpStatus.BAD_REQUEST.getReasonPhrase() )
                .message( "Erro de validação nos dados enviados" ).path( request.getRequestURI() ).details( details ).build();

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( error );
    }


    @ExceptionHandler( AuthenticationException.class )
    public ResponseEntity< ErrorResponse > handleAuthenticationException( AuthenticationException ex, HttpServletRequest request ) {

        log.error( "Erro de autenticação: {}", ex.getMessage() );

        ErrorResponse error = ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.UNAUTHORIZED.value() )
            .error( HttpStatus.UNAUTHORIZED.getReasonPhrase() ).message( "Credenciais inválidas" ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body( error );
    }


    @ExceptionHandler( BadCredentialsException.class )
    public ResponseEntity< ErrorResponse > handleBadCredentials( BadCredentialsException ex, HttpServletRequest request ) {

        log.error( "Credenciais inválidas: {}", ex.getMessage() );

        ErrorResponse error = ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.UNAUTHORIZED.value() )
            .error( HttpStatus.UNAUTHORIZED.getReasonPhrase() ).message( "Usuário ou senha inválidos" ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body( error );
    }


    @ExceptionHandler( AccessDeniedException.class )
    public ResponseEntity< ErrorResponse > handleAccessDenied( AccessDeniedException ex, HttpServletRequest request ) {

        log.error( "Acesso negado: {}", ex.getMessage() );

        ErrorResponse error =
            ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.FORBIDDEN.value() ).error( HttpStatus.FORBIDDEN.getReasonPhrase() )
                .message( "Você não tem permissão para acessar este recurso" ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( error );
    }


    @ExceptionHandler( Exception.class )
    public ResponseEntity< ErrorResponse > handleGenericException( Exception ex, HttpServletRequest request ) {

        log.error( "Erro interno do servidor: ", ex );

        ErrorResponse error = ErrorResponse.builder().timestamp( LocalDateTime.now() ).status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
            .error( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() ).message( "Erro interno do servidor" ).path( request.getRequestURI() ).build();

        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body( error );
    }
}