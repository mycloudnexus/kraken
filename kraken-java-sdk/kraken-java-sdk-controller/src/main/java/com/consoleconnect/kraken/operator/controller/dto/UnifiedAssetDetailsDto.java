package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnifiedAssetDetailsDto extends UnifiedAssetDto {
  private List<AssetLinkDto> assetLinks;
}
