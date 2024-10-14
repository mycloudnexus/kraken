package com.consoleconnect.kraken.operator.controller.dto;

import lombok.Data;

@Data
public class CreateTagRequest {
  private String name;
  private String description;
  private String tag;
}
