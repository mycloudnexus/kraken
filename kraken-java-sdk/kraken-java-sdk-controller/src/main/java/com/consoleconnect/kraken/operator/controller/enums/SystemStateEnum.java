package com.consoleconnect.kraken.operator.controller.enums;

import java.util.List;

public enum SystemStateEnum {
  CONTROL_PLANE_UPGRADING,
  CONTROL_PLANE_UPGRADE_DONE,
  STAGE_UPGRADING,
  STAGE_UPGRADE_DONE,
  PRODUCTION_UPGRADING,
  RUNNING;
  public static final List<String> CAN_UPGRADE_STATES =
      List.of(
          RUNNING.name(),
          SystemStateEnum.CONTROL_PLANE_UPGRADE_DONE.name(),
          SystemStateEnum.STAGE_UPGRADE_DONE.name());
}
