package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_MAPPING_MATRIX;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class MappingMatrixCheckerActionRunner extends AbstractActionRunner {
  public static final String MAPPING_MATRIX_KEY = "mappingMatrixKey";
  public static final String TARGET_KEY = "targetKey";
  public static final String MATRIX = "matrix";
  public static final String MESSAGE_ALERT = "api use case is not supported %s";
  public static final String CHECK_NAME_ENABLED = "enabled";
  public static final String PARAM_NAME = "param";
  private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;

  public MappingMatrixCheckerActionRunner(
      AppProperty appProperty,
      UnifiedAssetService unifiedAssetService,
      UnifiedAssetRepository unifiedAssetRepository) {
    super(appProperty);
    this.unifiedAssetService = unifiedAssetService;
    this.unifiedAssetRepository = unifiedAssetRepository;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.MAPPING_MATRIX_CHECKER
        == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  protected Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {
    onCheck(inputs);
    return Optional.empty();
  }

  protected void onCheck(Map<String, Object> inputs) {
    Assert.notNull(inputs.get(MAPPING_MATRIX_KEY), "mappingMatrixKey must not be null");
    Assert.notNull(inputs.get(MAPPING_MATRIX_KEY), "targetKey must not be null");
    String componentKey = inputs.get(MAPPING_MATRIX_KEY).toString();
    String targetKey = inputs.get(TARGET_KEY).toString();
    if (targetKey.contains(ResponseCodeTransform.TARGET_KEY_NOT_FOUND)) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":possibly product not supported"));
    }

    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                PRODUCT_MAPPING_MATRIX.getKind(),
                AssetsConstants.FIELD_KEY,
                componentKey),
            null,
            null,
            PageRequest.of(0, 1),
            null);
    // <mapper-key,<checkName,(path, expected)
    Map<String, List<PathCheck>> facets =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(assetDtoPaging.getData().get(0).getFacets().get(MATRIX)),
            new TypeReference<>() {});
    if (Objects.isNull(facets) || !facets.containsKey(targetKey)) {
      throw KrakenException.badRequest(
          MESSAGE_ALERT.formatted(":lack in check rules for target key: " + targetKey));
    }
    // check enable/disable
    Optional<PathCheck> enabledOpt =
        facets.get(targetKey).stream()
            .filter(check -> CHECK_NAME_ENABLED.equalsIgnoreCase(check.name))
            .findFirst();
    if (enabledOpt.isPresent() && enabledOpt.get().expected.equals("false")) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":disabled"));
    }
    if (!unifiedAssetRepository.findOneByKey(targetKey).isPresent()) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(":not deployed"));
    }
    DocumentContext documentContext = JsonPath.parse(inputs);
    StringBuilder builder = new StringBuilder();
    boolean allMatch =
        facets.get(targetKey).stream()
            .allMatch(
                pathCheckEntry -> {
                  boolean check = check(documentContext, pathCheckEntry);
                  log.info("Evaluate {} : {}", pathCheckEntry.name, check);
                  if (!check) {
                    builder.append(
                        pathCheckEntry.errorMsg != null
                            ? String.format(": %s", pathCheckEntry.errorMsg)
                            : String.format(
                                "item:%s,expected:%s; ",
                                pathCheckEntry.name, pathCheckEntry.expected));
                  }
                  return check;
                });
    if (!allMatch) {
      throw KrakenException.badRequest(MESSAGE_ALERT.formatted(builder));
    }
  }

  private boolean check(DocumentContext documentContext, PathCheck pathCheck) {
    Object realValue;
    try {
      realValue = documentContext.read(pathCheck.path());
    } catch (Exception e) {
      log.error("read json path error!");
      realValue = StringUtils.EMPTY;
    }
    if (realValue instanceof JSONArray array) {
      return array.stream().anyMatch(value -> checkExpect(pathCheck, value.toString()));
    }
    String s =
        realValue == null || StringUtils.isBlank(String.valueOf(realValue))
            ? StringUtils.EMPTY
            : String.valueOf(realValue);
    return checkExpect(pathCheck, s);
  }

  private boolean checkExpect(PathCheck pathCheck, String value) {
    if (pathCheck.expectedTrue != null) {
      return Boolean.TRUE.equals(
          SpELEngine.evaluate(pathCheck.expectedTrue, Map.of(PARAM_NAME, value), Boolean.class));
    }
    return pathCheck.expected.equalsIgnoreCase(value);
  }

  public record PathCheck(
      String name, String path, String expected, String expectedTrue, String errorMsg) {}
}
