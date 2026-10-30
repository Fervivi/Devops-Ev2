package cl.duoc.devops.minibiblioteca.controller;

import cl.duoc.devops.minibiblioteca.exception.LibroNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LibroNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> libroNoEncontrado(LibroNoEncontradoException ex) {
        return Map.of("error", ex.getMessage());
    }
}
