package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReportConfigReloadEvent {
  private String productReleaseId;
  private String status;
  private List<DeployComponentError> errors;

  public static ReportConfigReloadEvent of(
      String productReleaseId, List<DeployComponentError> errors) {
    Optional<DeployComponentError> fatal =
        errors.stream().filter(e -> e.getSeverity() == ErrorSeverityEnum.FATAL).findFirst();
    if (fatal.isPresent()) {
      return new ReportConfigReloadEvent(productReleaseId, DeployStatusEnum.FAILED.name(), errors);
    } else {
      return new ReportConfigReloadEvent(productReleaseId, DeployStatusEnum.SUCCESS.name(), errors);
    }
  }
}
