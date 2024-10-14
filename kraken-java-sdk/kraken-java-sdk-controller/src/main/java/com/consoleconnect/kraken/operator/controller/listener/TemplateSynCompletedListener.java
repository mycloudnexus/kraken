package com.consoleconnect.kraken.operator.controller.listener;

import com.consoleconnect.kraken.operator.controller.dto.CreateAPIMapperDeploymentRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateProductDeploymentRequest;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeRecord;
import com.consoleconnect.kraken.operator.controller.event.TemplateSynCompletedEvent;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.model.TemplateUpgradeDeploymentFacets;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.controller.service.TemplateUpgradeService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.AssetLink;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class TemplateSynCompletedListener {

  public static final int PAGE_SIZE = 200;
  private final UnifiedAssetService unifiedAssetService;
  private final ComponentTagService componentTagService;
  private final ProductDeploymentService productDeploymentService;
  private final EnvironmentService environmentService;
  private final TemplateUpgradeService templateUpgradeService;

  TemplateSynCompletedListener(
      UnifiedAssetService unifiedAssetService,
      ComponentTagService componentTagService,
      EnvironmentService environmentService,
      TemplateUpgradeService templateUpgradeService,
      ProductDeploymentService productDeploymentService) {
    this.unifiedAssetService = unifiedAssetService;
    this.componentTagService = componentTagService;
    this.productDeploymentService = productDeploymentService;
    this.environmentService = environmentService;
    this.templateUpgradeService = templateUpgradeService;
  }

  @EventListener(TemplateSynCompletedEvent.class)
  @Transactional
  public IngestionDataResult listenPlatformSettingCompletedEvent(TemplateSynCompletedEvent event) {
    return deploy(event);
  }

  protected IngestionDataResult deploy(TemplateSynCompletedEvent event) {
    List<String> keyList = new ArrayList<>();
    keyList.addAll(event.getTemplateUpgradeRecords().stream().map(UpgradeRecord::key).toList());
    if (CollectionUtils.isEmpty(keyList)) {
      return IngestionDataResult.of(200, "");
    }
    Map<String, String> mapper2TargetMap = new HashMap<>();
    Paging<UnifiedAssetDto> componentAssetDtoPaging =
        getUnifiedAssetDtoPaging(AssetKindEnum.COMPONENT_API.getKind());
    Paging<UnifiedAssetDto> mapperAssetDtoPaging =
        getUnifiedAssetDtoPaging(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind());
    Paging<UnifiedAssetDto> targetAssetDtoPaging =
        getUnifiedAssetDtoPaging(AssetKindEnum.COMPONENT_API_TARGET.getKind());
    Map<String, UnifiedAssetDto> mapperAssetDtoMap =
        mapperAssetDtoPaging.getData().stream()
            .collect(Collectors.toMap(t -> t.getMetadata().getKey(), Function.identity()));
    targetAssetDtoPaging
        .getData()
        .forEach(
            target ->
                mapper2TargetMap.put(
                    target.getMetadata().getMapperKey(), target.getMetadata().getKey()));

    Map<String, String> link2ComponentMap = Maps.newHashMap();
    componentAssetDtoPaging
        .getData()
        .forEach(
            asset -> {
              for (AssetLink link : asset.getLinks()) {
                link2ComponentMap.put(link.getTargetAssetKey(), asset.getMetadata().getKey());
              }
            });
    Iterator<String> iterator = keyList.iterator();

    Set<String> dealSet = new HashSet<>();
    Set<String> changedMappers = new HashSet<>();
    // merged mapper
    while (iterator.hasNext()) {
      String key = iterator.next();
      if (mapperAssetDtoMap.containsKey(key)) {
        changedMappers.add(key);
        dealSet.add(key);
        dealSet.add(mapper2TargetMap.get(key));
        dealSet.add(link2ComponentMap.get(key));
      }
    }
    Iterator<String> iterator1 = keyList.iterator();
    while (iterator1.hasNext()) {
      String key = iterator1.next();
      if (dealSet.contains(key)) {
        iterator1.remove();
      }
    }
    UnifiedAssetDto productAsset = templateUpgradeService.getProductAsset();
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(event.getTemplateUpgradeId());
    TemplateUpgradeDeploymentFacets templateUpgradeDeploymentFacets =
        UnifiedAsset.getFacets(assetDto, TemplateUpgradeDeploymentFacets.class);
    TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment =
        new TemplateUpgradeDeploymentFacets.EnvDeployment();
    templateUpgradeDeploymentFacets.setEnvDeployment(envDeployment);
    envDeployment.setEnvId(event.getEnvId());
    // build auto release for system template
    if (CollectionUtils.isNotEmpty(keyList)) {
      List<UnifiedAssetDto> finalAssets = generateDeployedAssets(keyList);
      String deploymentId =
          deploySystemTemplateDeployment(productAsset, finalAssets, event.getEnvId());
      envDeployment.setSystemDeployments(Arrays.asList(deploymentId));
    }
    if (CollectionUtils.isNotEmpty(changedMappers)) {
      deployMapperDeployment(
          link2ComponentMap, productAsset, changedMappers, event.getEnvId(), envDeployment);
    }

    Environment environment = environmentService.findOne(event.getEnvId());
    UnifiedAsset templateUpgradeDeployment =
        templateUpgradeService.newTemplateUpgradeDeployment(
            environment, event.getTemplateUpgradeId());

    Map<String, Object> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(templateUpgradeDeploymentFacets), new TypeReference<>() {});
    templateUpgradeDeployment.setFacets(facets);
    if (CollectionUtils.isEmpty(envDeployment.getMapperDeployment())
        || CollectionUtils.isEmpty(envDeployment.getSystemDeployments())) {
      templateUpgradeDeployment.getMetadata().setStatus(DeployStatusEnum.SUCCESS.name());
    } else {
      templateUpgradeDeployment.getMetadata().setStatus(DeployStatusEnum.IN_PROCESS.name());
    }

    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(
            assetDto.getId(),
            templateUpgradeDeployment,
            new SyncMetadata("", "", DateTime.nowInUTCString(), event.getUserId()),
            true);
    UUID id = ingestionDataResult.getData().getId();
    templateUpgradeService.addLabels(envDeployment, id.toString());
    return ingestionDataResult;
  }

  private Paging<UnifiedAssetDto> getUnifiedAssetDtoPaging(String kind) {
    return unifiedAssetService.search(null, kind, true, null, PageRequest.of(0, PAGE_SIZE));
  }

  private List<UnifiedAssetDto> generateDeployedAssets(List<String> keyList) {
    List<UnifiedAssetDto> finalAssets = unifiedAssetService.findByAllKeysIn(keyList, true);
    List<UnifiedAssetEntity> parentAssets =
        unifiedAssetService.findAllByIdIn(
            finalAssets.stream()
                .map(UnifiedAssetDto::getParentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
    Map<String, String> id2KeyMap =
        parentAssets.stream()
            .collect(
                Collectors.toMap(entity -> entity.getId().toString(), UnifiedAssetEntity::getKey));
    finalAssets.forEach(t -> t.setParentId(id2KeyMap.get(t.getParentId())));
    return finalAssets;
  }

  private String deploySystemTemplateDeployment(
      UnifiedAssetDto productAsset, List<UnifiedAssetDto> finalAssets, String envId) {

    IngestionDataResult systemTemplateTag =
        componentTagService.createSystemTemplateTag(
            productAsset.getMetadata().getKey(), finalAssets);
    CreateProductDeploymentRequest createProductDeploymentRequest =
        new CreateProductDeploymentRequest();
    createProductDeploymentRequest.setTagIds(
        Collections.singletonList(systemTemplateTag.getData().getId().toString()));
    createProductDeploymentRequest.setEnvId(envId);
    String format = DateTime.format(ZonedDateTime.now());
    createProductDeploymentRequest.setDescription("system-template:" + format);
    UnifiedAssetDto unifiedAssetDto =
        productDeploymentService.deployComponents(
            productAsset.getMetadata().getKey(),
            createProductDeploymentRequest,
            ReleaseKindEnum.SYSTEM_TEMPLATE_MIXED,
            templateUpgradeService.getSystemUpgradeUser(),
            false);
    return unifiedAssetDto.getId();
  }

  private void deployMapperDeployment(
      Map<String, String> link2ComponentMap,
      UnifiedAsset product,
      Set<String> changedMappers,
      String envId,
      TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment) {

    List<String> deploymentIds = new ArrayList<>();
    List<UnifiedAssetDto> byAllKeysIn =
        unifiedAssetService.findByAllKeysIn(new ArrayList<>(changedMappers), false);
    byAllKeysIn.stream()
        .filter(
            ent ->
                MapUtils.isNotEmpty(ent.getMetadata().getLabels())
                    && LabelConstants.VALUE_DEPLOYED_STATUS_DEPLOYED.equalsIgnoreCase(
                        ent.getMetadata().getLabels().get(LabelConstants.LABEL_DEPLOYED_STATUS)))
        .map(UnifiedAssetDto::getMetadata)
        .map(Metadata::getKey)
        .forEach(
            mapperKey -> {
              CreateAPIMapperDeploymentRequest request = new CreateAPIMapperDeploymentRequest();
              request.setMapperKeys(Arrays.asList(mapperKey));
              request.setComponentId(link2ComponentMap.get(mapperKey));
              request.setEnvId(envId);
              UnifiedAssetDto mapperVersionAndDeploy =
                  productDeploymentService.createMapperVersionAndDeploy(
                      product.getMetadata().getKey(),
                      request,
                      ReleaseKindEnum.API_LEVEL,
                      templateUpgradeService.getSystemUpgradeUser(),
                      true);
              deploymentIds.add(mapperVersionAndDeploy.getId());
            });
    if (CollectionUtils.isNotEmpty(deploymentIds)) {
      envDeployment.setMapperDeployment(deploymentIds);
    }
    List<String> draftMappers =
        byAllKeysIn.stream()
            .filter(
                ent ->
                    !(MapUtils.isNotEmpty(ent.getMetadata().getLabels())
                        && LabelConstants.VALUE_DEPLOYED_STATUS_DEPLOYED.equalsIgnoreCase(
                            ent.getMetadata()
                                .getLabels()
                                .get(LabelConstants.LABEL_DEPLOYED_STATUS))))
            .map(UnifiedAssetDto::getMetadata)
            .map(Metadata::getKey)
            .toList();
    if (CollectionUtils.isNotEmpty(draftMappers)) {
      envDeployment.setMapperDraft(draftMappers);
    }
  }
}
