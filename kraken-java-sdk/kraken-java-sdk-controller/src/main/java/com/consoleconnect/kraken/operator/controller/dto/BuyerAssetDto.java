package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuyerAssetDto extends UnifiedAssetDto {

  private BuyerToken buyerToken;

  @Data
  public static class BuyerToken {
    private String accessToken;
    private Date expiredAt;
  }
}
