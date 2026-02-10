package com.consoleconnect.kraken.operator.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class BuildVersionService {

  private final String appVersion;

  private final String buildRevision;

  public BuildVersionService(
      @Value("${spring.build.version}") String appVersion,
      @Value("${app.revision:-}") @Nullable String buildRevision) {
    this.appVersion = appVersion;
    this.buildRevision = buildRevision;
  }

  public String getAppVersion() {
    return this.appVersion;
  }

  public String getBuildRevision() {
    return this.buildRevision;
  }
}
