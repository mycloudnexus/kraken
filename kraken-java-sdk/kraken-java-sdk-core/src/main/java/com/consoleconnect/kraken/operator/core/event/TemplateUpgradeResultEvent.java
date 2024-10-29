package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeResultEventEnum;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class TemplateUpgradeResultEvent {
  String publishAssetKey;
  String productReleaseKey;
  ZonedDateTime receivedAt;
  ZonedDateTime installedAt;
  ZonedDateTime upgradeBeginAt;
  ZonedDateTime upgradeEndAt;
  EnvNameEnum envName;
  UpgradeResultEventEnum resultEventType;
}
