package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.API_USE_CASES;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_APP_VERSION;

import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KrakenVersionSpecificationBuildService {
  public static final String KRAKEN_PREFIX = "kraken@";
  public static final String KRAKEN_APP_NAME_PREFIX = "Kraken App ";
  public static final String FILE_NAME = KRAKEN_PREFIX + "%s.yaml";
  public static final String SUFFIX_API_VERSION = "api-version";

  @Value("${spring.build.version}")
  private String buildVersion;

  private final SystemInfoService systemInfoService;
  private final ApiComponentService apiComponentService;
  private final UnifiedAssetRepository unifiedAssetRepository;

  public ResponseEntity<Mono<Resource>> buildKrakenVersionSpecification()
      throws JsonProcessingException {
    Map<String, UnifiedAssetEntity> componentEntityMap =
        unifiedAssetRepository
            .findByKindOrderByCreatedAtDesc(AssetKindEnum.COMPONENT_API.getKind())
            .stream()
            .collect(Collectors.toMap(UnifiedAssetEntity::getKey, t -> t));
    SystemInfo systemInfo = systemInfoService.find();
    String key = KRAKEN_PREFIX + buildVersion;
    String name = KRAKEN_APP_NAME_PREFIX + buildVersion;
    Map<String, Object> unifiedAsset = new LinkedHashMap<>();

    unifiedAsset.put(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_APP_KRAKEN.getKind());
    unifiedAsset.put(FIELD_API_VERSION, "v1");
    LinkedHashMap<Object, Object> metadata = new LinkedHashMap<>();
    unifiedAsset.put(FIELD_METADATA, metadata);
    Map<String, String> labels = new LinkedHashMap<>();
    metadata.put(AssetsConstants.FIELD_NAME, name);
    metadata.put(AssetsConstants.FIELD_KEY, key);
    metadata.put(AssetsConstants.FIELD_LABELS, labels);

    labels.put(LABEL_APP_VERSION, systemInfo.getControlAppVersion());
    labels.put(LabelConstants.LABEL_PRODUCT_VERSION, systemInfo.getControlProductVersion());
    labels.put(LabelConstants.LABEL_PRODUCT_KEY, systemInfo.getProductKey());
    labels.put(LabelConstants.LABEL_PRODUCT_SPEC, systemInfo.getProductSpec());
    labels.put(
        LabelConstants.LABEL_PUBLISH_DATE,
        DateTime.nowInUTC().toLocalDate().format(DateTimeFormatter.ISO_DATE));
    metadata.put(AssetsConstants.FIELD_VERSION, NumberUtils.INTEGER_ONE);
    List<Map<String, Object>> list =
        apiComponentService.listAllApiUseCase().stream()
            .map(
                dto -> {
                  Map<String, Object> component = new LinkedHashMap<>();
                  component.put(AssetsConstants.FIELD_NAME, dto.getComponentName());
                  component.put(AssetsConstants.FIELD_KEY, dto.getComponentKey());
                  component.put(
                      AssetsConstants.FIELD_API_VERSION,
                      componentEntityMap.get(dto.getComponentKey()).getLabels().entrySet().stream()
                          .filter(entry -> entry.getKey().contains(SUFFIX_API_VERSION))
                          .findFirst()
                          .map(Map.Entry::getValue)
                          .orElse(""));
                  List<Map<String, Object>> endPoints =
                      dto.getDetails().stream()
                          .map(
                              detail -> {
                                Map<String, Object> endpoint = new LinkedHashMap<>();
                                endpoint.put(AssetsConstants.PATH, detail.getPath());
                                endpoint.put(AssetsConstants.METHOD, detail.getMethod());
                                endpoint.put(
                                    AssetsConstants.MAPPING_MATRIX, detail.getMappingMatrix());
                                return endpoint;
                              })
                          .toList();
                  component.put(AssetsConstants.END_POINTS, endPoints);
                  return component;
                })
            .toList();
    unifiedAsset.put(SPEC, Map.of(API_USE_CASES, list));
    YamlToolkit.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    String s = YamlToolkit.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(unifiedAsset);
    YamlToolkit.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    String fileName = String.format(FILE_NAME, buildVersion);
    Resource inputStreamResource =
        new InputStreamResource(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    Mono<Resource> resourceMono = Mono.just(inputStreamResource);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .body(resourceMono);
  }
}
