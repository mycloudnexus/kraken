package com.consoleconnect.kraken.operator.controller.service.upgrade;

import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import java.util.List;

public interface UpgradeSourceService {
  List<UpgradeTuple> getTemplateUpgradeRecords(String templateUpgradeId);

  String supportedUpgradeSource();

  void reportResult(String templateUpgradeId, String templateDeploymentId);

  List<ComponentExpandDTO> listApiUseCases(String templateId);
}
