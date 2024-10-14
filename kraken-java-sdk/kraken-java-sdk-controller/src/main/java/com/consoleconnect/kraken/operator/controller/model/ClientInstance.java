package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientInstance extends AbstractModel {
  private String name;
  private String clientId;
  private UnifiedAsset product;
  private List<UnifiedAsset> components;
}
