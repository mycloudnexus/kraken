package com.consoleconnect.kraken.operator.controller.config.vault;

import lombok.Data;

@Data
public class VaultProperty {
  private String namespace = "";

  private SecretMaterials secretMaterials = new SecretMaterials();

  @Data
  public static class SecretMaterials {
    private String buyerTokenPath = "/buyer-token/%s";
  }
}
