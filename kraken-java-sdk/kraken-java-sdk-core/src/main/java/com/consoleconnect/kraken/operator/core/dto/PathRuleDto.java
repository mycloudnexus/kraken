package com.consoleconnect.kraken.operator.core.dto;

import java.util.List;
import lombok.Data;

@Data
public class PathRuleDto {
  private String name;
  private String checkPath;
  private String deletePath;
  private List<KVPair> insertPath;

  @Data
  public static class KVPair {
    private String key;
    private String val;
  }
}
