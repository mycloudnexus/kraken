package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;

@Data
public class HttpResponse<T> {
  private int code;
  private String message;
  private T data;

  public static <T> HttpResponse<T> of(int code, String message, T data) {
    HttpResponse<T> res = new HttpResponse<>();
    res.setCode(code);
    res.setMessage(message);
    res.setData(data);
    return res;
  }

  public static <T> HttpResponse<T> ok(T data) {
    return of(200, "OK", data);
  }

  public static HttpResponse<Void> error(int code, String message) {
    return of(code, message, null);
  }
}
