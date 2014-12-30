package com.risevision.storage.api.exception;

public class ValidationException extends Exception {
  private static final long serialVersionUID = 1L;

  public ValidationException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(Throwable throwable) {
    super(throwable);
  }
}
