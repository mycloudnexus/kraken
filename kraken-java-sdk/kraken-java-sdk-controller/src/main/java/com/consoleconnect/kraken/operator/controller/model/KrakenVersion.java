package com.consoleconnect.kraken.operator.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KrakenVersion {

  private String buildVersion;

  private String appVersion;
}
