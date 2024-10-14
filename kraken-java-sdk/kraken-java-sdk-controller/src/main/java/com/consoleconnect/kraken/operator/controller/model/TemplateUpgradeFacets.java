package com.consoleconnect.kraken.operator.controller.model;

import java.util.List;
import lombok.Data;

@Data
public class TemplateUpgradeFacets {
  Version templateVersion;

  @Data
  public static class Version {
    private String templateVersion;
    String versionName;
    List<String> descriptions;
    String lowestCompatibleVersion;
  }
}
