package com.consoleconnect.kraken.operator.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileContentDescriptor {
  private String fullPath;
  private String content;
  private String sha;
}
