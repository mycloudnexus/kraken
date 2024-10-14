package com.consoleconnect.kraken.operator.controller.dto;

public record UpgradeRecord(String key, String kind, Integer version, String fullPath) {}
