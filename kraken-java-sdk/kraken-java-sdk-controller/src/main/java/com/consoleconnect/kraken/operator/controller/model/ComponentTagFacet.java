package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import java.util.List;
import lombok.Data;

@Data
public class ComponentTagFacet {
  public static final String KEY_COMPONENT = "component";
  public static final String KEY_CHILDREN = "children";
  UnifiedAssetDto component;
  List<UnifiedAssetDto> children;
}
