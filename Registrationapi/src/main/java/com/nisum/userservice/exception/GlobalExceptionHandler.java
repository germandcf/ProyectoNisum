package com.nisum.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import com.nisum.userservice.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        logger.error("Token expirado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Token de autenticación expirado");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex) {
        logger.error("Token mal formado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("El token de autenticación tiene un formato inválido");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({SignatureException.class, UnsupportedJwtException.class})
    public ResponseEntity<ErrorResponse> handleInvalidJwtException(Exception ex) {
        logger.error("Token inválido: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Token de autenticación inválido");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        logger.error("Error de usuario: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String message = ex.getReason();
        logger.error("Mensaje original: {}", message);
        
        if (message != null) {
            // Si el mensaje contiene el prefijo HTTP, extraer solo el mensaje
            if (message.contains("400 BAD_REQUEST")) {
                message = message.substring(message.indexOf("\"") + 1, message.lastIndexOf("\""));
            }
        }
        
        logger.error("Mensaje procesado: {}", message);
        ErrorResponse errorResponse = new ErrorResponse(message != null ? message : "Error desconocido");
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Error en el formato de la solicitud: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Error en el formato de la solicitud");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Error interno del servidor: ", ex);
        return new ResponseEntity<>(
            new ErrorResponse("Error interno del servidor: " + ex.getMessage()),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        logger.error("Tipo de medio no soportado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Tipo de medio no soportado");
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Error de validación: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Error de validación en los datos");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.error("Método no permitido: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Método no permitido");
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.warn("Recurso no encontrado: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        ErrorResponse error = new ErrorResponse("El recurso solicitado no existe");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.error("Error de tipo de argumento: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Error en el tipo de datos");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        logger.error("Parámetro faltante: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Faltan parámetros requeridos");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
} 