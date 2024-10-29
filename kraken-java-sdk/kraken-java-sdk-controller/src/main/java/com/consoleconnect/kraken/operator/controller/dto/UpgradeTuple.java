package com.consoleconnect.kraken.operator.controller.dto;

import java.util.List;

public record UpgradeTuple(
    List<UpgradeRecord> versionChangedTemplates,
    List<UpgradeRecord> enforceUpgradeTemplates,
    List<UpgradeRecord> directSaves,
    String productKey) {}
