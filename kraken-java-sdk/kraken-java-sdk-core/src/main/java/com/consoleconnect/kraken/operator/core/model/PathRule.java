package com.consoleconnect.kraken.operator.core.model;

import java.util.List;
import lombok.Data;

@Data
public class PathRule {
  private String name;
  private String checkPath;
  private String deletePath;
  private List<KVPair> insertPath;
}
