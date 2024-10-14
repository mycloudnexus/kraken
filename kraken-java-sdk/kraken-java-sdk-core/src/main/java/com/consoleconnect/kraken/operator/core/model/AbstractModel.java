package com.consoleconnect.kraken.operator.core.model;

import lombok.Data;

@Data
public class AbstractModel {
  private String id;
  private String productId;
  private String createdAt;
  private String createdBy;
  private String updatedAt;
  private String updatedBy;
}
