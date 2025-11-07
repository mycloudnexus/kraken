package com.consoleconnect.kraken.operator.auth.exception;

import com.consoleconnect.kraken.operator.auth.dto.ValidationErrorDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ValidationErrorDto> handleConstraintViolationException(
      WebExchangeBindException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getFieldErrors()
        .forEach(violation -> errors.put(violation.getField(), violation.getDefaultMessage()));
    return new ResponseEntity<>(
        ValidationErrorDto.builder().message("Validation errors").fieldErrors(errors).build(),
        HttpStatus.BAD_REQUEST);
  }
}
