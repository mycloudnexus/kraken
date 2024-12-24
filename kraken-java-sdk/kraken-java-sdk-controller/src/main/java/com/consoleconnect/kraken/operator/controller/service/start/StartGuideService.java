package com.consoleconnect.kraken.operator.controller.service.start;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum.COMPLETE;
import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.controller.dto.start.ApiMappingInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.DeploymentInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.SellerApiServerRegistrationInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.StartGuideInfoDto;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StartGuideService {

  public static final int DEFAULT_NUMBER_OF_COMPLETED_MAPPINGS = 2;
  public static final String PRODUCT_ID_CAN_NOT_BE_EMPTY_ERROR = "Product id can not be empty.";

  private final ApiComponentService apiComponentService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final UnifiedAssetService unifiedAssetService;
  private final EnvironmentService environmentService;
  private final ProductDeploymentService productDeploymentService;

  public StartGuideInfoDto getStartGuideInfo(String productId) {
    validateParams(productId);
    var atLeastOneSellerApiRegistered = atLeastOneSellerApiRegistered(productId);
    var atLeastOneMappingCompleted = atLeastOneMappingCompleted();
    var atLestOnBuyerRegistered = atLestOnBuyerRegistered(productId);
    var atLeastOneApiDeployedOnStage = atLeastOneApiDeployedToEvn(productId, EnvNameEnum.STAGE);
    var atLeastOneApiDeployedOnProduction =
        atLeastOneApiDeployedToEvn(productId, EnvNameEnum.PRODUCTION);

    return new StartGuideInfoDto(
        new SellerApiServerRegistrationInfoDto(atLeastOneSellerApiRegistered),
        new ApiMappingInfoDto(atLeastOneMappingCompleted),
        new DeploymentInfoDto(
            atLeastOneApiDeployedOnStage,
            atLestOnBuyerRegistered,
            atLeastOneApiDeployedOnProduction));
  }

  private void validateParams(String productId) {
    if (StringUtils.isBlank(productId)) {
      throw new KrakenException(400, PRODUCT_ID_CAN_NOT_BE_EMPTY_ERROR);
    }
  }

  private boolean atLeastOneSellerApiRegistered(String productId) {
    var parentId = unifiedAssetService.findOne(productId).getId();
    return unifiedAssetRepository.existsByParentIdAndKind(
        parentId, COMPONENT_API_TARGET_SPEC.getKind());
  }

  private boolean atLeastOneMappingCompleted() {
    var completedMappings =
        apiComponentService.listAllApiUseCase().stream()
            .flatMap(component -> component.getDetails().stream())
            .filter(mapping -> mapping.getMappingStatus().equalsIgnoreCase(COMPLETE.getDesc()))
            .toList();
    return completedMappings.size() > DEFAULT_NUMBER_OF_COMPLETED_MAPPINGS;
  }

  private boolean atLestOnBuyerRegistered(String productId) {
    var parentId = unifiedAssetService.findOneByIdOrKey(productId).getId().toString();
    return unifiedAssetRepository.existsByParentIdAndKind(parentId, PRODUCT_BUYER.getKind());
  }

  private boolean atLeastOneApiDeployedToEvn(String productId, EnvNameEnum envNameEnum) {
    var environments =
        environmentService
            .search(productId, getSearchPageRequest(0, EnvNameEnum.values().length))
            .getData();
    Optional<Environment> environment =
        environments.stream()
            .filter(e -> envNameEnum.name().equalsIgnoreCase(e.getName()))
            .findFirst();
    return environment
        .map(
            env -> {
              var deployments =
                  productDeploymentService.retrieveApiMapperDeployments(
                      environment.get().getId(),
                      null,
                      DeployStatusEnum.SUCCESS,
                      getSearchPageRequest(0, 1));
              return !deployments.getData().isEmpty();
            })
        .orElse(false);
  }
}
