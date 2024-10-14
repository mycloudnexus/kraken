package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.util.List;
import lombok.Data;

@Data
public class ComponentWitheVersionDTO extends AbstractModel {
  String key;
  String name;
  List<ComponentVersionDto> componentVersions;
}
