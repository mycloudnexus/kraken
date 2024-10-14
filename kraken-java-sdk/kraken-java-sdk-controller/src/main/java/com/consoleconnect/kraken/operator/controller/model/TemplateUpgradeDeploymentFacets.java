package com.consoleconnect.kraken.operator.controller.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TemplateUpgradeDeploymentFacets {
  EnvDeployment envDeployment;

  @Data
  public static class EnvDeployment {
    String envId;
    List<String> systemDeployments = new ArrayList<>();
    List<String> mapperDeployment = new ArrayList<>();
    List<String> mapperDraft = new ArrayList<>();
  }
}
