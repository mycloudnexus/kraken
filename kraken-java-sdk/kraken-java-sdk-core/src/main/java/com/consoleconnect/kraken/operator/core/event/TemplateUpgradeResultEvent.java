package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeResultEventEnum;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class TemplateUpgradeResultEvent {
  String templateKey;
  ZonedDateTime receivedAt;
  ZonedDateTime installedAt;
  ZonedDateTime upgradeBeginAt;
  ZonedDateTime upgradeEndAt;
  EnvNameEnum envName;
  String productKey;
  String productSpec;
  String productVersion;
  UpgradeResultEventEnum resultEventType;
}
