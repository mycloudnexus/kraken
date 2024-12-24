package com.consoleconnect.kraken.operator.controller.service.start;

import static com.consoleconnect.kraken.operator.controller.service.start.StartGuideService.PRODUCT_ID_CAN_NOT_BE_EMPTY_ERROR;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;
import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum.COMPLETE;
import static com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum.INCOMPLETE;
import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;
import static com.consoleconnect.kraken.operator.core.toolkit.PagingHelper.toPage;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.controller.dto.ApiMapperDeploymentDTO;
import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StartGuideServiceTest {

  public static final String STAGE_ENVIRONMENT_ID_1 = "eId1";
  public static final String PRODUCTION_ENVIRONMENT_ID_2 = "eId2";
  public static final String PRODUCT_ID = "productId1";
  public static final String KIND = COMPONENT_API_TARGET_SPEC.getKind();
  public static final String ASSET_PRODUCT_ID_1 = "assetProductId1";

  private StartGuideService sut;
  private ApiComponentService apiComponentServiceMock;
  private UnifiedAssetRepository unifiedAssetRepositoryMock;
  private UnifiedAssetService unifiedAssetServiceMock;
  private EnvironmentService environmentServiceMock;
  private ProductDeploymentService productDeploymentServiceMock;

  private UnifiedAssetDto product;

  @BeforeEach
  void setUp() {
    apiComponentServiceMock = mock(ApiComponentService.class);
    unifiedAssetRepositoryMock = mock(UnifiedAssetRepository.class);
    unifiedAssetServiceMock = mock(UnifiedAssetService.class);
    environmentServiceMock = mock(EnvironmentService.class);
    productDeploymentServiceMock = mock(ProductDeploymentService.class);
    sut =
        new StartGuideService(
            apiComponentServiceMock,
            unifiedAssetRepositoryMock,
            unifiedAssetServiceMock,
            environmentServiceMock,
            productDeploymentServiceMock);

    product = new UnifiedAssetDto();
    product.setParentId(ASSET_PRODUCT_ID_1);
  }

  @Test
  void givenNoSellerApiRegistered_whenGettingAtLeastOneSellerApiRegistered_thenReturnsFalse() {
    // given
    givenNoBuyer();
    givenNoApiDeployedOnEnvs();
    when(unifiedAssetServiceMock.findOne(PRODUCT_ID)).thenReturn(product);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(product.getId(), KIND))
        .thenReturn(false);
    // when
    var result = sut.getStartGuideInfo(PRODUCT_ID);
    // then
    assertThat(result.getSellerApiServerRegistrationInfo().getAtLeastOneSellerApiRegistered())
        .isFalse();
  }

  @Test
  void
      givenAtLeastOneSellerApiRegistered_whenGettingAtLeastOneSellerApiRegistered_thenReturnsTrue() {
    // given
    givenNoBuyer();
    givenNoApiDeployedOnEnvs();
    when(unifiedAssetServiceMock.findOne(StartGuideServiceTest.PRODUCT_ID)).thenReturn(product);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(product.getId(), KIND))
        .thenReturn(true);
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    assertThat(result.getSellerApiServerRegistrationInfo().getAtLeastOneSellerApiRegistered())
        .isTrue();
  }

  @Test
  void givenNoApiMappingCompleted_whenGettingAtLeastOneMappingCompleted_thenReturnsFalse() {
    // given
    givenNoSellerApiRegistered(product);
    givenNoApiDeployedOnEnvs();
    givenNoBuyer();
    when(apiComponentServiceMock.listAllApiUseCase()).thenReturn(Collections.emptyList());
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    var apiMappingInfo = result.getApiMappingInfo();
    assertThat(apiMappingInfo.getAtLeastOneMappingCompleted()).isFalse();
  }

  @Test
  void
      givenOnlyDefaultApiMappingCompleted_whenGettingAtLeastOneMappingCompleted_thenReturnsFalse() {
    // given
    givenNoSellerApiRegistered(product);
    givenNoApiDeployedOnEnvs();
    givenNoBuyer();

    var componentExpandDTO1 = new ComponentExpandDTO();
    componentExpandDTO1.setDetails(
        of(targetMappingDetail(COMPLETE), targetMappingDetail(INCOMPLETE)));
    var componentExpandDTO2 = new ComponentExpandDTO();
    componentExpandDTO2.setDetails(of(targetMappingDetail(COMPLETE)));
    when(apiComponentServiceMock.listAllApiUseCase())
        .thenReturn(of(componentExpandDTO1, componentExpandDTO2));
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    var apiMappingInfo = result.getApiMappingInfo();
    assertThat(apiMappingInfo.getAtLeastOneMappingCompleted()).isFalse();
  }

  @Test
  void
      givenDefaultApiMappingAndOneCompleted_whenGettingAtLeastOneMappingCompleted_thenReturnsTrue() {
    // given
    givenNoSellerApiRegistered(product);
    givenNoApiDeployedOnEnvs();
    givenNoBuyer();

    var componentExpandDTO1 = new ComponentExpandDTO();
    componentExpandDTO1.setDetails(
        of(
            targetMappingDetail(COMPLETE),
            targetMappingDetail(COMPLETE),
            targetMappingDetail(INCOMPLETE)));
    var componentExpandDTO2 = new ComponentExpandDTO();
    componentExpandDTO2.setDetails(of(targetMappingDetail(COMPLETE)));
    when(apiComponentServiceMock.listAllApiUseCase())
        .thenReturn(of(componentExpandDTO1, componentExpandDTO2));
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    var apiMappingInfo = result.getApiMappingInfo();
    assertThat(apiMappingInfo.getAtLeastOneMappingCompleted()).isTrue();
  }

  @Test
  void givenNoBuyerRegistered_whenGettingAtLestOnBuyerRegistered_thenReturnsFalse() {
    // given
    var assetEntity = new UnifiedAssetEntity();
    var parentId = UUID.randomUUID();
    assetEntity.setId(parentId);
    givenNoSellerApiRegistered(product);
    givenNoApiDeployedOnEnvs();

    when(unifiedAssetServiceMock.findOneByIdOrKey(StartGuideServiceTest.PRODUCT_ID))
        .thenReturn(assetEntity);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(
            parentId.toString(), PRODUCT_BUYER.getKind()))
        .thenReturn(false);
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    assertThat(result.getDeploymentInfo().getAtLeastOneBuyerRegistered()).isFalse();
  }

  @Test
  void givenAtLeastOneBuyerRegistered_whenGettingAtLestOnBuyerRegistered_thenReturnsTrue() {
    // given
    var parentId = UUID.randomUUID();
    var assetEntity = new UnifiedAssetEntity();
    assetEntity.setId(parentId);
    givenNoSellerApiRegistered(product);
    givenNoApiDeployedOnEnvs();
    when(unifiedAssetServiceMock.findOneByIdOrKey(StartGuideServiceTest.PRODUCT_ID))
        .thenReturn(assetEntity);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(
            parentId.toString(), PRODUCT_BUYER.getKind()))
        .thenReturn(true);
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    assertThat(result.getDeploymentInfo().getAtLeastOneBuyerRegistered()).isTrue();
  }

  @Test
  void givenNoApiDeployedToEnvs_whenGettingAtLeastOneApiDeployedToEnv_thenReturnsFalse() {
    // given
    givenNoSellerApiRegistered(product);
    givenNoBuyer();

    givenNoApiDeployedOnEnvs();

    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    assertThat(result.getDeploymentInfo().getAtLeastOneApiDeployedToStage()).isFalse();
    assertThat(result.getDeploymentInfo().getAtLeastOneApiDeployedToProduction()).isFalse();
  }

  @Test
  void givenAtLeastOneApiDeployedToEnvs_whenGettingAtLeastOneApiDeployedToEnv_thenReturnsTrue() {
    // given
    givenNoSellerApiRegistered(product);
    givenNoBuyer();

    when(environmentServiceMock.search(
            StartGuideServiceTest.PRODUCT_ID, getSearchPageRequest(0, EnvNameEnum.values().length)))
        .thenReturn(toPage(environments(), 0, 2));
    when(productDeploymentServiceMock.retrieveApiMapperDeployments(
            STAGE_ENVIRONMENT_ID_1, null, DeployStatusEnum.SUCCESS, getSearchPageRequest(0, 1)))
        .thenReturn(
            PagingHelper.toPage(
                of(firstDefaultApiMapper(), secondDefaultApiMapper(), new ApiMapperDeploymentDTO()),
                0,
                3));
    when(productDeploymentServiceMock.retrieveApiMapperDeployments(
            PRODUCTION_ENVIRONMENT_ID_2,
            null,
            DeployStatusEnum.SUCCESS,
            getSearchPageRequest(0, 1)))
        .thenReturn(
            PagingHelper.toPage(
                of(firstDefaultApiMapper(), secondDefaultApiMapper(), new ApiMapperDeploymentDTO()),
                0,
                3));
    // when
    var result = sut.getStartGuideInfo(StartGuideServiceTest.PRODUCT_ID);
    // then
    var deploymentInfo = result.getDeploymentInfo();
    assertThat(deploymentInfo.getAtLeastOneApiDeployedToStage()).isTrue();
    assertThat(deploymentInfo.getAtLeastOneApiDeployedToProduction()).isTrue();
  }

  @Test
  void givenNoProductId_whenGetStartGuideInfo_thenReturnsError() {
    // given
    // when
    var ex =
        assertThrows(
            KrakenException.class,
            () -> {
              sut.getStartGuideInfo("");
            });

    // then
    assertThat(ex.getCode()).isEqualTo(400);
    assertThat(ex.getMessage()).isEqualTo(PRODUCT_ID_CAN_NOT_BE_EMPTY_ERROR);
  }

  private ApiMapperDeploymentDTO secondDefaultApiMapper() {
    return new ApiMapperDeploymentDTO();
  }

  private ApiMapperDeploymentDTO firstDefaultApiMapper() {
    return new ApiMapperDeploymentDTO();
  }

  private List<Environment> environments() {
    Environment e1 = new Environment();
    e1.setId(STAGE_ENVIRONMENT_ID_1);
    e1.setName(EnvNameEnum.STAGE.name());
    Environment e2 = new Environment();
    e2.setName(EnvNameEnum.PRODUCTION.name());
    e2.setId(PRODUCTION_ENVIRONMENT_ID_2);
    return of(e1, e2);
  }

  private ComponentExpandDTO.TargetMappingDetail targetMappingDetail(
      MappingStatusEnum mappingStatus) {
    var targetMappingDetail = new ComponentExpandDTO.TargetMappingDetail();
    targetMappingDetail.setMappingStatus(mappingStatus.getDesc());
    return targetMappingDetail;
  }

  private void givenNoSellerApiRegistered(UnifiedAssetDto product) {
    when(unifiedAssetServiceMock.findOne(StartGuideServiceTest.PRODUCT_ID)).thenReturn(product);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(
            product.getId(), StartGuideServiceTest.KIND))
        .thenReturn(false);
  }

  private void givenNoApiDeployedOnEnvs() {
    when(environmentServiceMock.search(
            StartGuideServiceTest.PRODUCT_ID, getSearchPageRequest(0, EnvNameEnum.values().length)))
        .thenReturn(toPage(environments(), 0, 2));
    when(productDeploymentServiceMock.retrieveApiMapperDeployments(
            STAGE_ENVIRONMENT_ID_1, null, DeployStatusEnum.SUCCESS, getSearchPageRequest(0, 1)))
        .thenReturn(PagingHelper.toPage(Collections.emptyList(), 0, 0));
    when(productDeploymentServiceMock.retrieveApiMapperDeployments(
            PRODUCTION_ENVIRONMENT_ID_2,
            null,
            DeployStatusEnum.SUCCESS,
            getSearchPageRequest(0, 1)))
        .thenReturn(PagingHelper.toPage(Collections.emptyList(), 0, 0));
  }

  private void givenNoBuyer() {
    UnifiedAssetEntity assetEntity = new UnifiedAssetEntity();
    assetEntity.setId(UUID.randomUUID());
    when(unifiedAssetServiceMock.findOneByIdOrKey(StartGuideServiceTest.PRODUCT_ID))
        .thenReturn(assetEntity);
    when(unifiedAssetRepositoryMock.existsByParentIdAndKind(
            assetEntity.getId().toString(), PRODUCT_BUYER.getKind()))
        .thenReturn(false);
  }
}
