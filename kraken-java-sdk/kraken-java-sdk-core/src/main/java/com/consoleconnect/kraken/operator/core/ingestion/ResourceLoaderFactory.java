package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ResourceLoaderFactory {

  private final List<AbstractResourceLoader> loaders;

  public Optional<FileContentDescriptor> readFile(String fullPath) {
    for (AbstractResourceLoader loader : loaders) {
      if (fullPath.startsWith(loader.getResourcePrefix())) {
        return loader.readFile(fullPath);
      }
    }
    return Optional.empty();
  }
}
