package com.consoleconnect.kraken.operator.config;

import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppMgmtProperty {
  private List<Product> products;
  private List<SellerContact> sellerContacts;

  @Getter
  @Setter
  public static class Product {
    private String key;
    private List<CreateEnvRequest> environments;
  }

  @Data
  public static class SellerContact {
    private String key;
    private List<CreateSellerContactRequest> sellerContactDetails;
  }
}
