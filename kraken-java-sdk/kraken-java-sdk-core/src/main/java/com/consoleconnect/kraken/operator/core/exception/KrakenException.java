package com.consoleconnect.kraken.operator.core.exception;

import lombok.Getter;

@Getter
public class KrakenException extends RuntimeException {

  private static final String NOT_FOUND_MSG = "404 Not Found";
  private static final String NOT_FOUND_DESC =
      "The requested URL was not found on the server. If you entered the URL manually please check your spelling and try again.";
  private static final String UNPROCESSABLE_ENTITY_MSG = "422 UnProcessable Entity";

  private final int code;

  public KrakenException(int code) {
    this.code = code;
  }

  public KrakenException(int code, String message) {
    super(message);
    this.code = code;
  }

  public KrakenException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public KrakenException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public static KrakenException badRequest(String message) {
    return new KrakenException(400, message);
  }

  public static KrakenException notFound(String message) {
    return new KrakenException(404, message);
  }

  public static KrakenException notFound(String message, Throwable cause) {
    return new KrakenException(404, message, cause);
  }

  public static KrakenException notFoundDefault() {
    return new KrakenException(404, NOT_FOUND_MSG, new IllegalArgumentException(NOT_FOUND_DESC));
  }

  public static KrakenException unProcessableEntity(String message) {
    return new KrakenException(
        422, UNPROCESSABLE_ENTITY_MSG, new IllegalArgumentException(message));
  }

  public static KrakenException unauthorized(String message) {
    return new KrakenException(401, message);
  }

  public static KrakenException forbidden(String message) {
    return new KrakenException(403, message);
  }

  public static KrakenException internalError(String message) {
    return new KrakenException(500, message);
  }

  public static KrakenException notImplemented(String message) {
    return new KrakenException(501, message);
  }
}
