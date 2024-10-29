package com.consoleconnect.kraken.operator.config;

import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppMgmtProperty {
  private List<Product> products;

  @Getter
  @Setter
  public static class Product {
    private String key;
    private List<CreateEnvRequest> environments;
  }
}
