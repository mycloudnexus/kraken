package com.consoleconnect.kraken.operator.core.ingestion.fs;

import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractResourceLoader;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileResourceLoader extends AbstractResourceLoader {
  @Override
  public String getResourcePrefix() {
    return ResourceLoaderTypeEnum.FILE.getKind();
  }

  public Optional<FileContentDescriptor> readFile(String fullPath) {
    return defaultResourceLoader(fullPath);
  }
}
