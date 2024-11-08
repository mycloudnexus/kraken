package com.consoleconnect.kraken.operator.controller.api;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_APP_VERSION;

import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping(value = "/products/{productId}")
@Tag(
    name = "Kraken Version Specification Builder",
    description = "Kraken Version Specification Builder")
@Slf4j
@RequiredArgsConstructor
@Controller
public class KrakenApiUseCaseBuilderController {
  public static final String KRAKEN_PREFIX = "kraken@";
  public static final String KRAKEN_APP_NAME_PREFIX = "Kraken App ";
  public static final String FILE_NAME = KRAKEN_PREFIX + "%s.yaml";

  @Value("${spring.build.version}")
  private String buildVersion;

  private final SystemInfoService systemInfoService;
  private final ApiComponentService apiComponentService;
  private final UnifiedAssetRepository unifiedAssetRepository;

  @SneakyThrows
  @Operation(summary = "generate version specification")
  @GetMapping("/version-specification")
  public ResponseEntity<Mono<Resource>> versionSpecification() {
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
                      componentEntityMap.get(dto.getComponentKey()).getApiVersion());
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
