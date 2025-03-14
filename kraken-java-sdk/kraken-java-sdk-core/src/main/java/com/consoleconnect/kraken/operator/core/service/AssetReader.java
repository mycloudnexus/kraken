package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

public interface AssetReader {

  @Slf4j
  final class LogHolder {}

  ResourceLoaderFactory getResourceLoaderFactory();

  default Optional<UnifiedAsset> readFromPath(String fullPath) {
    Optional<FileContentDescriptor> fileContentDescriptor =
        getResourceLoaderFactory().readFile(fullPath);
    if (fileContentDescriptor.isEmpty()) {
      LogHolder.log.warn("Unified Asset Not Found,Path: {}", fullPath);
      return Optional.empty();
    }
    return YamlToolkit.parseYaml(fileContentDescriptor.get().getContent(), UnifiedAsset.class);
  }
}
