package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public abstract class AbstractAssetEventListener {

  private final ApplicationEventPublisher publisher;

  @Getter private final AppProperty appProperty;

  protected AbstractAssetEventListener(
      ApplicationEventPublisher publisher, AppProperty appProperty) {
    this.publisher = publisher;
    this.appProperty = appProperty;
  }

  public final void publishIngestDataEvent(Object event) {
    publisher.publishEvent(event);
  }

  public abstract AssetKindEnum getKind();

  public void onPrePersist(
      FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    // do nothing
  }

  public void onPostPersist(
      String parentId, FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    // do nothing
  }
}
