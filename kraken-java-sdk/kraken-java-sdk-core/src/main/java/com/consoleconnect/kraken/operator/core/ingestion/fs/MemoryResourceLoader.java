package com.consoleconnect.kraken.operator.core.ingestion.fs;

import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractResourceLoader;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceCacheHolder;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class MemoryResourceLoader extends AbstractResourceLoader {
  private final ResourceCacheHolder resourceCacheHolder;

  @Override
  public String getResourcePrefix() {
    return ResourceLoaderTypeEnum.MEMORY.getKind();
  }

  @Override
  public Optional<FileContentDescriptor> readFile(String fullPath) {
    String content = resourceCacheHolder.get(fullPath);
    if (content == null) {
      return Optional.empty();
    }
    FileContentDescriptor fileContentDescriptor = new FileContentDescriptor();
    fileContentDescriptor.setContent(content);
    fileContentDescriptor.setSha(generateCheckSum(content.getBytes()));
    fileContentDescriptor.setFullPath(fullPath);
    return Optional.of(fileContentDescriptor);
  }
}
