package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
public abstract class AbstractResourceLoader {

  public abstract String getResourcePrefix();

  public abstract Optional<FileContentDescriptor> readFile(String fullPath);

  public static String generateCheckSum(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data);
      byte[] digest = md.digest();
      return DatatypeConverter.printHexBinary(digest).toUpperCase();
    } catch (Exception ex) {
      return Base64.getEncoder().encodeToString(data);
    }
  }

  public Optional<FileContentDescriptor> defaultResourceLoader(String fullPath) {
    log.info("Reading data from: {}", fullPath);
    try {
      // resource loader
      ResourceLoader resourceLoader = new DefaultResourceLoader();
      Resource resource = resourceLoader.getResource(fullPath);
      String data = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

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
