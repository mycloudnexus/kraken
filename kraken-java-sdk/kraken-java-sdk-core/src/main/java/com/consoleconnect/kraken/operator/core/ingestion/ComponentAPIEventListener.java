package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ComponentAPIEventListener extends AbstractAssetEventListener {

  public ComponentAPIEventListener(
      ApplicationEventPublisher eventPublisher, AppProperty appProperty) {
    super(eventPublisher, appProperty);
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API;
  }

  @Override
  public void onPrePersist(
      FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    // do nothing here
  }
}
