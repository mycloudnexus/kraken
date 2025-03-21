package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenDeploymentException;
import java.util.List;
import java.util.UUID;
import joptsimple.internal.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeploymentErrorHelperTest {

  @Test
  void testDeploymentHelper() {
    UnifiedAssetDto assetDto = new UnifiedAssetDto();
    assetDto.setId(UUID.randomUUID().toString());

    String errorMsg = "Failed to deploy workflow";
    KrakenDeploymentException fatalException =
        KrakenDeploymentException.internalFatalError(String.format(errorMsg));
    DeployComponentError fatal = DeployComponentError.of(assetDto, fatalException);

    KrakenDeploymentException warningException =
        KrakenDeploymentException.internalWarningError(Strings.EMPTY);
    DeployComponentError warning = DeployComponentError.of(assetDto, warningException);

    KrakenDeploymentException noticeException =
        KrakenDeploymentException.internalNoticeError(Strings.EMPTY);
    DeployComponentError notice = DeployComponentError.of(assetDto, noticeException);

    KrakenDeploymentException noticeException2 = KrakenDeploymentException.internalNoticeError();
    DeployComponentError notice2 = DeployComponentError.of(assetDto, noticeException2);

    DeployComponentError common = DeployComponentError.of(assetDto, new Exception(Strings.EMPTY));
    DeployComponentError reason =
        DeploymentErrorHelper.extractFailReason(List.of(fatal, warning, notice, notice2, common));

    Assertions.assertNotNull(reason);
    Assertions.assertEquals(errorMsg, reason.getReason());
  }
}
