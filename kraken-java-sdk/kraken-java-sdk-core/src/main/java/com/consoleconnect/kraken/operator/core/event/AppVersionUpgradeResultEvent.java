package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class AppVersionUpgradeResultEvent {
  private String appVersion;
  EnvNameEnum envName;
  private ZonedDateTime upgradeAt;
}
