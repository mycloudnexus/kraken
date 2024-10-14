package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.util.List;
import lombok.Data;

@Data
public class ComponentVersionDto extends AbstractModel {
  String name;
  String version;
  String key;
  String componentName;
  List<UnifiedAssetDto> assetDtoList;

  @Override
  public String toString() {
    return "ComponentVersionDto{"
        + "name='"
        + name
        + '\''
        + ", version='"
        + version
        + '\''
        + ", key='"
        + key
        + '\''
        + ", componentName='"
        + componentName
        + '\''
        + '}';
  }
}
