package com.consoleconnect.kraken.operator.core.model.facet;

import com.consoleconnect.kraken.operator.core.enums.APISpecContentFormatEnum;
import lombok.Data;

@Data
public class ComponentAPISpecFacets {
  public static final String FACET_API_BASE_SPEC = "baseSpec";
  public static final String FACET_API_CUSTOMIZED_SPEC = "customizedSpec";
  private APISpec baseSpec;
  private APISpec customizedSpec;

  @Data
  public static class APISpec {
    private String path;
    private String content;
    private APISpecContentFormatEnum format = APISpecContentFormatEnum.OPEN_API;
  }
}
