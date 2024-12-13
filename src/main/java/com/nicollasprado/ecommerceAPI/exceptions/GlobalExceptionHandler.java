package com.nicollasprado.ecommerceAPI.exceptions;

import com.nicollasprado.ecommerceAPI.services.exceptions.ObjectNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER") // Logger da classe no terminal
@RestControllerAdvice // Aplica os metodos de controle de exeçoes para todos controllers na inicializaçao da api
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${server.error.include-exception}") // @Value faz referencia ao "application-properties"
    private boolean printStackTrace;


    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // Define o tipo da exceçao
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException methodArgumentNotValidException,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validaçao, confira o campo 'errors' para detalhes.");
        // Adiciona todas exceçoes ocorridas na List errors do objeto errorResponse
        for(FieldError fieldError : methodArgumentNotValidException.getBindingResult().getFieldErrors()){
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllUncaughtException(Exception exception, WebRequest request){
        final String errorMsg = "Um erro desconhecido foi detectado!";
        log.error(errorMsg, exception); // Log do slf4j do lombok
        return buildErrorResponse(exception, errorMsg, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // Define o tipo de exceçao que esta classe ira controlar
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException dataIntegrityViolationException,
            WebRequest request){
        String errorMsg = dataIntegrityViolationException.getMostSpecificCause().getMessage();
        log.error("Falha ao salvar entidade com problemas de integridade: " + errorMsg, dataIntegrityViolationException);
        return buildErrorResponse(dataIntegrityViolationException, errorMsg, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException constraintViolationException, WebRequest request) {
        log.error("Falha ao validar elemento: " + constraintViolationException);
        return buildErrorResponse(constraintViolationException, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleObjectNotFoundException(ObjectNotFoundException objectNotFoundException, WebRequest request){
        log.error("Falha ao encontrar objeto requisitado: ", objectNotFoundException);
        return buildErrorResponse(objectNotFoundException, HttpStatus.NOT_FOUND, request);
    }


    private ResponseEntity<Object> buildErrorResponse(
            Exception exception,
            HttpStatus httpStatus,
            WebRequest request){
        return buildErrorResponse(exception, exception.getMessage(), httpStatus, request);
    }

    private ResponseEntity<Object> buildErrorResponse(
            Exception exception,
            String message,
            HttpStatus httpStatus,
            WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), message);
        if(this.printStackTrace){ // Checa se esta em produçao (se o printStackTrace esta ativado ou nao)
            errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
        }
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
