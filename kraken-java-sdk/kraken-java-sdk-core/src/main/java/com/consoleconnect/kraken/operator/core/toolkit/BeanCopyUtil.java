package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.time.ZonedDateTime;
import java.util.Optional;

public final class BeanCopyUtil {
  private BeanCopyUtil() {}

  public static void copyOperationInfo(AbstractEntity entity, AbstractModel model) {
    model.setCreatedBy(entity.getCreatedBy());
    model.setCreatedAt(entity.getCreatedAt().toString());
    model.setUpdatedBy(entity.getUpdatedBy());
    model.setUpdatedAt(
        Optional.ofNullable(entity.getUpdatedAt()).map(ZonedDateTime::toString).orElse(null));
  }

  public static void copyOperationInfo(UnifiedAssetDto entity, AbstractModel model) {
    model.setCreatedBy(entity.getCreatedBy());
    model.setCreatedAt(entity.getCreatedAt().toString());
    model.setUpdatedBy(entity.getUpdatedBy());
    model.setUpdatedAt(entity.getUpdatedAt());
  }
}
