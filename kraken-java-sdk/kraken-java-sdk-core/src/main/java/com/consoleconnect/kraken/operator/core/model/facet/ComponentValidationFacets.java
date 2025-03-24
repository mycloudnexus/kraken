package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.List;
import lombok.Data;

@Data
public class ComponentValidationFacets {

  private List<ModificationRule> modificationRules;

  @Data
  public static class ModificationRule {
    private String useCase;
    private String referenceMapper;
    private List<CompareItem> allowedChanges;
    private List<CompareItem> restrictedChanges;
  }

  @Data
  public static class CompareItem {
    private String sourceItem;
    private String targetItem;
  }
}
