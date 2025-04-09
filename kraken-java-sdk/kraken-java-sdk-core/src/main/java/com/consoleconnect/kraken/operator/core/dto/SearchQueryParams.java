package com.consoleconnect.kraken.operator.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchQueryParams {
  private String parentId;
  private String kind;
  private boolean facetIncluded;
  private String query;
  private String parentProductType;
}
