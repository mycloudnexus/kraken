package com.consoleconnect.kraken.operator.toolkit;

import com.consoleconnect.kraken.operator.controller.tools.DeploymentHelper;
import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenDeploymentException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeploymentHelperTest {

  @Test
  void testDeploymentHelper() {
    UnifiedAssetDto assetDto = new UnifiedAssetDto();
    assetDto.setId(UUID.randomUUID().toString());

    String errorMsg = "Failed to deploy workflow";
    KrakenDeploymentException fatalException =
        KrakenDeploymentException.internalFatalError(String.format(errorMsg));
    DeployComponentError fatal = DeployComponentError.of(assetDto, fatalException);

    KrakenDeploymentException warningException =
        KrakenDeploymentException.internalWarningError(String.format(""));
    DeployComponentError warning = DeployComponentError.of(assetDto, warningException);

    KrakenDeploymentException noticeException =
        KrakenDeploymentException.internalNoticeError(String.format(""));
    DeployComponentError notice = DeployComponentError.of(assetDto, noticeException);

    KrakenDeploymentException noticeException2 = KrakenDeploymentException.internalNoticeError();
    DeployComponentError notice2 = DeployComponentError.of(assetDto, noticeException2);

    DeployComponentError common = DeployComponentError.of(assetDto, new Exception(""));
    DeployComponentError reason =
        DeploymentHelper.extractFailReason(List.of(fatal, warning, notice, notice2, common));

    Assertions.assertNotNull(reason);
    Assertions.assertEquals(errorMsg, reason.getReason());
  }
}
