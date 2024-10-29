package com.consoleconnect.kraken.operator.controller.event;

public record SingleMapperReportEvent(
    String envId, String mapperKey, String version, String subVersion) {}
