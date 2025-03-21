package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;

public class DeploymentErrorHelper {

  private DeploymentErrorHelper() {}

  private static final int MAX_REASON_MSG = 120;

  private static final String STRING_TRUNCATE_MARK = "...";

  public static DeployComponentError extractFailReason(List<DeployComponentError> errors) {
    if (CollectionUtils.isEmpty(errors)) {
      return null;
    }
    Optional<DeployComponentError> fatal =
        errors.stream().filter(e -> e.getSeverity() == ErrorSeverityEnum.FATAL).findFirst();
    if (fatal.isEmpty()) {
      return null;
    }
    if (fatal.get().getReason().length() > MAX_REASON_MSG) {
      fatal
          .get()
          .setReason(fatal.get().getReason().substring(0, MAX_REASON_MSG) + STRING_TRUNCATE_MARK);
    }
    return fatal.get();
  }
}
