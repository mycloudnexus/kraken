package com.consoleconnect.kraken.operator.test;

import org.testcontainers.containers.PostgreSQLContainer;

public class KrakenPostgresqlContainer extends PostgreSQLContainer<KrakenPostgresqlContainer> {

  private static final String IMAGE_VERSION = "postgres:14.9";
  private static KrakenPostgresqlContainer container;

  public KrakenPostgresqlContainer() {
    super(IMAGE_VERSION);
  }

  public static KrakenPostgresqlContainer getInstance() {
    if (container == null) {
      container = new KrakenPostgresqlContainer();
      container.addFixedExposedPort(6432, 5432);
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("spring.datasource.url", container.getJdbcUrl());
    System.setProperty("spring.datasource.username", container.getUsername());
    System.setProperty("spring.datasource.password", container.getPassword());
  }

  @Override
  public void stop() {
    // do nothing, JVM handles shut down
  }
}
