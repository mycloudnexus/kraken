package com.consoleconnect.kraken.operator.core.ingestion.fs;

import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.ingestion.AbstractResourceLoader;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RawDataResourceLoader extends AbstractResourceLoader {
  @Override
  public String getResourcePrefix() {
    return ResourceLoaderTypeEnum.RAW.getKind();
  }

  public Optional<FileContentDescriptor> readFile(String fullPath) {
    log.info("Reading raw data from classpath: {}", fullPath);
    try {
      String data = fullPath.substring(4);
      FileContentDescriptor fileContentDescriptor = new FileContentDescriptor();
      fileContentDescriptor.setContent(data);
      fileContentDescriptor.setSha(generateCheckSum(data.getBytes()));
      fileContentDescriptor.setFullPath(fullPath);
      return Optional.of(fileContentDescriptor);
    } catch (Exception ex) {
      log.error("Error reading data from: {},error:{}", fullPath, ex.getMessage());
      return Optional.empty();
    }
  }
}
