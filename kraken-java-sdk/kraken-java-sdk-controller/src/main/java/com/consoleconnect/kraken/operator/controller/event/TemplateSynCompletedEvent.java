package com.consoleconnect.kraken.operator.controller.event;

import com.consoleconnect.kraken.operator.controller.dto.UpgradeRecord;
import java.util.List;
import lombok.Data;

@Data
public class TemplateSynCompletedEvent {
  private List<UpgradeRecord> templateUpgradeRecords;
  private String envId;
  private String templateUpgradeId;
  private String userId;
}
