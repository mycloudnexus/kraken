package com.consoleconnect.kraken.operator.auth.dto;

import lombok.Data;

@Data
public class JwtTokenDto {
  private Header header;
  private Payload payload;

  @Data
  public static class Header {
    private String kid;
    private String alg;
  }

  @Data
  public static class Payload {
    private String sub;
    private String iat;
    private String exp;
    private String iss;
  }
}
