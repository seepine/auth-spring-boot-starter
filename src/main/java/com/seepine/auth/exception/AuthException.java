package com.seepine.auth.exception;

import com.seepine.auth.enums.AuthExceptionType;

/**
 * @author seepine
 */
public class AuthException extends RuntimeException {
  String message;
  AuthExceptionType type;

  public AuthException(AuthExceptionType type) {
    this.type = type;
    this.message = type.message;
  }

  public AuthExceptionType getType() {
    return type;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
