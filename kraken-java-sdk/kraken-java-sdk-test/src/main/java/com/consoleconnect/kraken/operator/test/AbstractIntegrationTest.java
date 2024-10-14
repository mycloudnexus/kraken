package com.consoleconnect.kraken.operator.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractIntegrationTest {
  @Container
  static final KrakenPostgresqlContainer POSTGRESQL_CONTAINER =
      KrakenPostgresqlContainer.getInstance();

  public static String readFileToString(String path) throws IOException {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(path);
    return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
  }
}
