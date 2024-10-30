package com.consoleconnect.kraken.operator.controller.service.upgrade;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.UpgradeSourceEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpgradeSourceServiceFactory {
  private final List<UpgradeSourceService> upgradeSourceServices;
  private final UnifiedAssetService unifiedAssetService;

  public UpgradeSourceService getUpgradeSourceService(String templateUpgradeId) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(templateUpgradeId);
    String upgradeSource =
        assetDto
            .getMetadata()
            .getLabels()
            .getOrDefault(LabelConstants.LABEL_UPGRADE_SOURCE, UpgradeSourceEnum.CLASSPATH.name());
    return upgradeSourceServices.stream()
        .filter(service -> service.supportedUpgradeSource().equalsIgnoreCase(upgradeSource))
        .findFirst()
        .orElseThrow(() -> KrakenException.internalError("upgrade source handler not found"));
  }

  public UpgradeSourceService getUpgradeSourceService(UpgradeSourceEnum upgradeSourceEnum) {
    return upgradeSourceServices.stream()
        .filter(
            service -> service.supportedUpgradeSource().equalsIgnoreCase(upgradeSourceEnum.name()))
        .findFirst()
        .orElseThrow(() -> KrakenException.internalError("upgrade source handler not found"));
  }
}
