package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT;
import static com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum.IMPLEMENTATION_TARGET_MAPPER;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.*;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.AssetLinkEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TemplateUpgradeService {
  private final UnifiedAssetService unifiedAssetService;
  private final EnvironmentService environmentService;
  private final ProductDeploymentService productDeploymentService;
  private final UserService userService;

  @Transactional(rollbackFor = Exception.class)
  public String deployProduction(
      String templateUpgradeId, String stageEnvId, String productionEnvId, String userId) {
    // find stage template deployment
    UnifiedAssetDto stageDeployment = findStageDeployment(templateUpgradeId, stageEnvId);
    UnifiedAssetDto productAsset = getProductAsset();
    TemplateUpgradeDeploymentFacets templateUpgradeDeploymentFacets4Stage =
        UnifiedAsset.getFacets(stageDeployment, TemplateUpgradeDeploymentFacets.class);
    Environment environment = environmentService.findOne(productionEnvId);
    // new deployment for production
    UnifiedAsset templateUpgradeDeployment =
        newTemplateUpgradeDeployment(environment, templateUpgradeId);
    TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment =
        new TemplateUpgradeDeploymentFacets.EnvDeployment();
    // clone
    cloneMapperDeployment(
        templateUpgradeDeploymentFacets4Stage, productAsset, envDeployment, environment);
    TemplateUpgradeDeploymentFacets templateUpgradeDeploymentFacets =
        new TemplateUpgradeDeploymentFacets();
    templateUpgradeDeploymentFacets.setEnvDeployment(envDeployment);
    Map<String, Object> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(templateUpgradeDeploymentFacets), new TypeReference<>() {});
    templateUpgradeDeployment.setFacets(facets);
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(
            templateUpgradeId,
            templateUpgradeDeployment,
            new SyncMetadata("", "", DateTime.nowInUTCString(), userId),
            true);
    addLabels(envDeployment, ingestionDataResult.getData().getId().toString());
    return ingestionDataResult.getData().getId().toString();
  }

  private void cloneMapperDeployment(
      TemplateUpgradeDeploymentFacets templateUpgradeDeploymentFacets4Stage,
      UnifiedAssetDto productAsset,
      TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment,
      Environment production) {
    templateUpgradeDeploymentFacets4Stage
        .getEnvDeployment()
        .getMapperDeployment()
        .forEach(
            deploymentId -> {
              UnifiedAssetDto assetDto = unifiedAssetService.findOne(deploymentId);
              DeploymentFacet deploymentFacet =
                  UnifiedAsset.getFacets(assetDto, DeploymentFacet.class);
              for (ComponentTag componentTag : deploymentFacet.getComponentTags()) {
                CreateProductDeploymentRequest request = new CreateProductDeploymentRequest();
                request.setTagIds(List.of(componentTag.getTagId()));
                request.setEnvId(production.getId());
                UnifiedAssetDto unifiedAssetDto =
                    productDeploymentService.create(
                        productAsset.getMetadata().getKey(),
                        request,
                        ReleaseKindEnum.API_LEVEL,
                        getSystemUpgradeUser(),
                        true);
                envDeployment.getMapperDeployment().add(unifiedAssetDto.getId());
              }
            });
    templateUpgradeDeploymentFacets4Stage
        .getEnvDeployment()
        .getSystemDeployments()
        .forEach(
            deploymentId -> {
              UnifiedAssetDto assetDto = unifiedAssetService.findOne(deploymentId);
              DeploymentFacet deploymentFacet =
                  UnifiedAsset.getFacets(assetDto, DeploymentFacet.class);
              for (ComponentTag componentTag : deploymentFacet.getComponentTags()) {
                CreateProductDeploymentRequest request = new CreateProductDeploymentRequest();
                request.setTagIds(List.of(componentTag.getTagId()));
                request.setEnvId(production.getId());
                UnifiedAssetDto unifiedAssetDto =
                    productDeploymentService.create(
                        productAsset.getMetadata().getKey(),
                        request,
                        ReleaseKindEnum.SYSTEM_TEMPLATE_MIXED,
                        getSystemUpgradeUser(),
                        false);
                envDeployment.getSystemDeployments().add(unifiedAssetDto.getId());
              }
            });
  }

  private UnifiedAssetDto findStageDeployment(String templateUpgradeId, String stageEnvId) {
    List<UnifiedAssetDto> list =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND,
                    AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
                Tuple2.ofList(
                    LabelConstants.LABEL_ENV_ID,
                    stageEnvId,
                    LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID,
                    templateUpgradeId),
                null,
                null,
                null)
            .getData();
    assert CollectionUtils.isNotEmpty(list);
    return list.get(0);
  }

  public void addLabels(
      TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment,
      String templateUpgradeDeploymentId) {
    Stream.concat(
            envDeployment.getMapperDeployment().stream(),
            envDeployment.getSystemDeployments().stream())
        .filter(Objects::nonNull)
        .forEach(
            deploymentId ->
                unifiedAssetService.addLabel(
                    deploymentId,
                    LabelConstants.LABEL_APP_TEMPLATE_DEPLOYMENT_ID,
                    templateUpgradeDeploymentId));
  }

  public UnifiedAssetDto getProductAsset() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            List.of(new Tuple2(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT.getKind())),
            null,
            null,
            null,
            null);
    return assetDtoPaging.getData().get(0);
  }

  public UnifiedAsset newTemplateUpgradeDeployment(
      Environment environment, String templateUpgradeId) {
    String key = PRODUCT_TEMPLATE_DEPLOYMENT.getKind() + ":" + DateTime.nowInUTCString();
    UnifiedAsset templateUpgradeDeployment =
        UnifiedAsset.of(PRODUCT_TEMPLATE_DEPLOYMENT.getKind(), key, "template upgrade");
    templateUpgradeDeployment
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_ENV_ID, environment.getId());
    templateUpgradeDeployment
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_ENV_NAME, environment.getName());
    templateUpgradeDeployment
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, templateUpgradeId);
    return templateUpgradeDeployment;
  }

  public Paging<TemplateUpgradeDeploymentVO> listTemplateDeployment(
      String templateUpgradeIdParam, PageRequest pageRequest) {
    Paging<UnifiedAssetDto> templateDeploymentsPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            Optional.ofNullable(templateUpgradeIdParam)
                .map(t -> Tuple2.ofList(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, t))
                .orElse(null),
            null,
            pageRequest,
            null);
    List<String> templateUpgradeIds =
        templateDeploymentsPaging.getData().stream()
            .map(
                dto ->
                    dto.getMetadata().getLabels().get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID))
            .filter(StringUtils::isNotBlank)
            .toList();
    Map<String, String> templateUpgradeMap =
        unifiedAssetService.findAllByIdIn(templateUpgradeIds).stream()
            .collect(
                Collectors.toMap(
                    t -> t.getId().toString(),
                    t -> t.getLabels().get(LabelConstants.LABEL_RELEASE_VERSION)));
    List<TemplateUpgradeDeploymentVO> voList =
        templateDeploymentsPaging.getData().stream()
            .map(
                dto -> {
                  String templateUpgradeId =
                      dto.getMetadata()
                          .getLabels()
                          .get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID);
                  String envName = dto.getMetadata().getLabels().get(LabelConstants.LABEL_ENV_NAME);
                  TemplateUpgradeDeploymentVO templateUpgradeDeploymentVO =
                      new TemplateUpgradeDeploymentVO();
                  templateUpgradeDeploymentVO.setTemplateUpgradeId(templateUpgradeId);
                  templateUpgradeDeploymentVO.setUpgradeBy(dto.getCreatedBy());
                  templateUpgradeDeploymentVO.setEnvName(envName);
                  templateUpgradeDeploymentVO.setReleaseVersion(
                      templateUpgradeMap.get(templateUpgradeId));
                  templateUpgradeDeploymentVO.setStatus(dto.getMetadata().getStatus());
                  templateUpgradeDeploymentVO.setDeploymentId(dto.getId());
                  templateUpgradeDeploymentVO.setCreatedAt(dto.getCreatedAt());
                  return templateUpgradeDeploymentVO;
                })
            .toList();
    return PagingHelper.toPageNoSubList(
        voList,
        templateDeploymentsPaging.getPage(),
        templateDeploymentsPaging.getSize(),
        templateDeploymentsPaging.getTotal());
  }

  public List<MapperTagVO> templateDeploymentDetails(String templateDeploymentId) {
    UnifiedAssetDto templateDeployment = unifiedAssetService.findOne(templateDeploymentId);
    TemplateUpgradeDeploymentFacets upgradeDeploymentFacets =
        UnifiedAsset.getFacets(templateDeployment, TemplateUpgradeDeploymentFacets.class);
    List<MapperTagVO> draftList =
        upgradeDeploymentFacets.getEnvDeployment().getMapperDraft().stream()
            .map(
                mapperKey -> {
                  MapperTagVO mapperTagVO = new MapperTagVO();
                  mapperTagVO.setMapperKey(mapperKey);
                  List<AssetLinkEntity> assetLink =
                      unifiedAssetService.findAssetLink(
                          mapperKey, IMPLEMENTATION_TARGET_MAPPER.getKind());
                  AssetLinkEntity assetLinkEntity = assetLink.get(0);
                  mapperTagVO.setComponentKey(assetLinkEntity.getAsset().getKey());
                  UnifiedAssetDto mapperAsset = unifiedAssetService.findOne(mapperKey);
                  ComponentAPITargetFacets mapperFacets =
                      UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
                  ComponentExpandDTO.MappingMatrix mappingMatrix =
                      new ComponentExpandDTO.MappingMatrix();
                  BeanUtils.copyProperties(mapperFacets.getTrigger(), mappingMatrix);
                  mapperTagVO.setMappingMatrix(mappingMatrix);
                  mapperTagVO.setStatus(DeployStatusEnum.DRAFT.name());
                  return mapperTagVO;
                })
            .toList();
    List<MapperTagVO> deployedList =
        upgradeDeploymentFacets.getEnvDeployment().getMapperDeployment().stream()
            .flatMap(
                mapperDeploymentId -> {
                  UnifiedAssetDto mapperDeployment =
                      unifiedAssetService.findOne(mapperDeploymentId);
                  DeploymentFacet facets =
                      UnifiedAsset.getFacets(mapperDeployment, DeploymentFacet.class);
                  return facets.getComponentTags().stream()
                      .map(
                          tag -> {
                            UnifiedAssetDto assetDto = unifiedAssetService.findOne(tag.getTagId());
                            ComponentTagFacet componentTagFacet =
                                UnifiedAsset.getFacets(assetDto, ComponentTagFacet.class);
                            UnifiedAssetDto component = componentTagFacet.getComponent();
                            UnifiedAssetDto mapperAsset =
                                componentTagFacet.getChildren().stream()
                                    .filter(
                                        asset ->
                                            asset
                                                .getKind()
                                                .equalsIgnoreCase(
                                                    AssetKindEnum.COMPONENT_API_TARGET_MAPPER
                                                        .getKind()))
                                    .findFirst()
                                    .orElse(null);
                            MapperTagVO mapperTagVO = new MapperTagVO();
                            mapperTagVO.setTagId(tag.getTagId());
                            mapperTagVO.setMapperKey(mapperAsset.getMetadata().getKey());
                            mapperTagVO.setComponentKey(component.getMetadata().getKey());
                            mapperTagVO.setVersion(
                                assetDto
                                    .getMetadata()
                                    .getLabels()
                                    .get(LabelConstants.LABEL_VERSION_NAME));
                            ComponentAPITargetFacets mapperFacets =
                                UnifiedAsset.getFacets(mapperAsset, ComponentAPITargetFacets.class);
                            ComponentExpandDTO.MappingMatrix mappingMatrix =
                                new ComponentExpandDTO.MappingMatrix();
                            BeanUtils.copyProperties(mapperFacets.getTrigger(), mappingMatrix);
                            mapperTagVO.setMappingMatrix(mappingMatrix);
                            mapperTagVO.setStatus(mapperDeployment.getMetadata().getStatus());
                            return mapperTagVO;
                          });
                })
            .toList();
    List<MapperTagVO> resultList = Lists.newArrayList();
    resultList.addAll(deployedList);
    resultList.addAll(draftList);
    return resultList;
  }

  public List<TemplateUpgradeDeploymentVO> currentUpgradeVersion() {
    return environmentService.findAll().stream()
        .map(
            environment ->
                unifiedAssetService
                    .findBySpecification(
                        Tuple2.ofList(
                            AssetsConstants.FIELD_KIND, PRODUCT_TEMPLATE_DEPLOYMENT.getKind(),
                            AssetsConstants.FIELD_STATUS, DeployStatusEnum.SUCCESS.name()),
                        Tuple2.ofList(LabelConstants.LABEL_ENV_ID, environment.getId()),
                        null,
                        PageRequest.of(0, 1),
                        null)
                    .getData()
                    .stream()
                    .findFirst()
                    .map(
                        templateDeployment -> {
                          String templateUpgradeId =
                              templateDeployment
                                  .getMetadata()
                                  .getLabels()
                                  .get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID);
                          String version =
                              unifiedAssetService
                                  .findOne(templateUpgradeId)
                                  .getMetadata()
                                  .getLabels()
                                  .get(LabelConstants.LABEL_RELEASE_VERSION);
                          TemplateUpgradeDeploymentVO templateUpgradeDeploymentVO =
                              new TemplateUpgradeDeploymentVO();
                          templateUpgradeDeploymentVO.setEnvId(environment.getId());
                          templateUpgradeDeploymentVO.setEnvName(environment.getName());
                          templateUpgradeDeploymentVO.setTemplateUpgradeId(templateUpgradeId);
                          templateUpgradeDeploymentVO.setReleaseVersion(version);
                          templateUpgradeDeploymentVO.setTemplateUpgradeDeploymentId(
                              templateDeployment.getId());
                          return templateUpgradeDeploymentVO;
                        })
                    .orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }

  public void checkCondition2StageUpgrade(String templateUpgradeId, String envId) {
    Paging<UnifiedAssetDto> assetDtoPaging = findLatestTemplateUpgrade();
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      throw KrakenException.badRequest("no upgrade found");
    }
    UnifiedAssetDto templateUpgrade = assetDtoPaging.getData().get(0);
    if (!templateUpgrade.getId().equalsIgnoreCase(templateUpgradeId)) {
      throw KrakenException.badRequest("upgrade is not the latest ,operation is not allowed");
    }
    Paging<UnifiedAssetDto> deploymentPage =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            Tuple2.ofList(
                LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID,
                templateUpgradeId,
                LabelConstants.LABEL_ENV_ID,
                envId),
            null,
            PageRequest.of(0, 1),
            null);
    checkStatus(
        deploymentPage,
        dto -> {
          if (List.of(DeployStatusEnum.IN_PROCESS.name(), DeployStatusEnum.SUCCESS.name())
              .contains(dto.getMetadata().getStatus())) {
            throw KrakenException.badRequest("upgrade is already completed");
          }
        });
  }

  public void checkCondition2ProductionUpgrade(CreateProductionUpgradeRequest upgradeRequest) {
    Paging<UnifiedAssetDto> assetDtoPaging = findLatestTemplateUpgrade();
    if (CollectionUtils.isEmpty(assetDtoPaging.getData())) {
      throw KrakenException.badRequest("no upgrade found");
    }
    UnifiedAssetDto templateUpgrade = assetDtoPaging.getData().get(0);
    if (!templateUpgrade.getId().equalsIgnoreCase(upgradeRequest.getTemplateUpgradeId())) {
      throw KrakenException.badRequest("upgrade is not the latest ,operation is not allowed");
    }
    Paging<UnifiedAssetDto> deploymentPage =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            Tuple2.ofList(
                LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID,
                upgradeRequest.getTemplateUpgradeId(),
                LabelConstants.LABEL_ENV_ID,
                upgradeRequest.getStageEnvId()),
            null,
            PageRequest.of(0, 1),
            null);
    checkStatus(
        deploymentPage,
        dto -> {
          if (!DeployStatusEnum.SUCCESS.name().equalsIgnoreCase(dto.getMetadata().getStatus())) {
            throw KrakenException.badRequest("stage upgrade possibly not completed");
          }
        });
    Paging<UnifiedAssetDto> productionDeploymentPage =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(AssetsConstants.FIELD_KIND, PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            Tuple2.ofList(
                LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID,
                upgradeRequest.getTemplateUpgradeId(),
                LabelConstants.LABEL_ENV_ID,
                upgradeRequest.getProductEnvId()),
            null,
            PageRequest.of(0, 1),
            null);
    checkStatus(
        productionDeploymentPage,
        dto -> {
          if (List.of(DeployStatusEnum.IN_PROCESS.name(), DeployStatusEnum.SUCCESS.name())
              .contains(dto.getMetadata().getStatus())) {
            throw KrakenException.badRequest("production upgrade already existed");
          }
        });
  }

  private void checkStatus(
      Paging<UnifiedAssetDto> paging, Consumer<UnifiedAssetDto> deployStatusConsumer) {
    Optional.ofNullable(paging)
        .map(Paging::getData)
        .filter(CollectionUtils::isNotEmpty)
        .map(ListUtils::getFirst)
        .ifPresent(deployStatusConsumer::accept);
  }

  private Paging<UnifiedAssetDto> findLatestTemplateUpgrade() {
    return unifiedAssetService.findBySpecification(
        Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
        null,
        null,
        PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
        null);
  }

  public String getSystemUpgradeUser() {
    return userService.findOneByIdOrEmail(UserContext.SYSTEM).getId().toString();
  }
}
