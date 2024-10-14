package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;

@Data
public class Notification {
  private int id;
  private String type;
  private String envId;
  private String productId;
  private String version;

  private String message;

  private Object data;
}
