package com.seepine.auth.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author seepine
 * @since 0.0.1
 */
@RestControllerAdvice
public class AuthExceptionResponseBodyAdvice {

  @ExceptionHandler(AuthException.class)
  public String authException(AuthException e) {
    return e.getMessage();
  }
}
