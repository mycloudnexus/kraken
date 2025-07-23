package com.consoleconnect.kraken.operator.controller.config.vault;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VaultData<T> {

  private T data;
  private Metadata metadata;

  @Data
  public static class Metadata {
    @JsonProperty("created_time")
    private String createdTime;

    @JsonProperty("deletion_time")
    private String deletionTime;

    @JsonProperty("destroyed")
    private boolean destroyed;

    @JsonProperty("version")
    private int version;
  }
}
