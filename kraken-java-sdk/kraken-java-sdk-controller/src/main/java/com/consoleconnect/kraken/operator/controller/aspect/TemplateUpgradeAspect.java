package com.consoleconnect.kraken.operator.controller.aspect;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@AllArgsConstructor
public class TemplateUpgradeAspect {
  private final UnifiedAssetService unifiedAssetService;

  @Around(
      " @annotation(com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker)")
  public Object validateAspect(ProceedingJoinPoint pjp) throws Throwable {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind(),
                AssetsConstants.FIELD_STATUS,
                DeployStatusEnum.IN_PROCESS.name()),
            null,
            null,
            null,
            null);
    if (CollectionUtils.isNotEmpty(assetDtoPaging.getData())) {
      throw KrakenException.forbidden(
          "Due to ongoing(IN_PROCESS) deployments,update operations are not allowed");
    }
    return pjp.proceed();
  }
}
