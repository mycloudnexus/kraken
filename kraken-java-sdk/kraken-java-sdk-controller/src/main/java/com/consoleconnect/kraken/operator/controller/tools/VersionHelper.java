package com.consoleconnect.kraken.operator.controller.tools;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class VersionHelper {

  private static final String VERSION_SEPARATOR = "-";

  private static final String SNAPSHOT = "SNAPSHOT";

  private VersionHelper() {}

  public static String generateVersion(UnifiedAssetEntity unifiedAssetEntity) {
    if (Objects.isNull(unifiedAssetEntity)) {
      return Constants.INIT_VERSION;
    }
    return upgradeVersionByZeroPointOne(
        unifiedAssetEntity.getLabels().get(LabelConstants.LABEL_VERSION_NAME));
  }

  public static String upgradeVersionByZeroPointOne(String version) {
    if (StringUtils.isBlank(version)) {
      return Constants.INIT_VERSION;
    }
    return new BigDecimal(version)
        .add(BigDecimal.valueOf(0.1))
        .setScale(1, RoundingMode.HALF_UP)
        .toString();
  }
}
