package com.consoleconnect.kraken.operator.gateway.func;

import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.convertToJsonPointer;
import static com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer.RESPONSE_BODY;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import com.consoleconnect.kraken.operator.gateway.repo.HttpRequestRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("spelFunc")
public class SpelFunc {
  private final HttpRequestRepository httpRequestRepository;
  private final UnifiedAssetService unifiedAssetService;

  public SpelFunc(
      HttpRequestRepository httpRequestRepository, UnifiedAssetService unifiedAssetService) {
    this.httpRequestRepository = httpRequestRepository;
    this.unifiedAssetService = unifiedAssetService;
  }

  public static List<Object> appendSellerInformation(
      String role, String name, String emailAddress, String number, List<Object> list) {
    log.info("append seller information: name={}", name);
    Map<String, String> contact = new HashMap<>();
    contact.put("role", role);
    contact.put("name", name);
    contact.put("emailAddress", emailAddress);
    contact.put("number", number);
    list.add(contact);
    return list;
  }

  public String renderId(String sellerOrderId) {
    List<HttpRequestEntity> entities = httpRequestRepository.findByExternalId(sellerOrderId);
    if (CollectionUtils.isEmpty(entities)) {
      throw KrakenException.notFound("not found externalId in Kraken");
    }
    return entities.get(0).getId().toString();
  }

  public Map<String, Object> appendFromResponseMapping(
      Map<String, Object> map, String rootPath, String mapperKey) {
    UnifiedAssetDto asset = unifiedAssetService.findOne(mapperKey);
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    List<ComponentAPITargetFacets.Mapper> response =
        facets.getEndpoints().get(0).getMappers().getResponse();
    response.stream()
        .filter(mapper -> mapper.getTarget().contains(rootPath))
        .forEach(
            mapper -> {
              Map<String, Object> tmpMap = new HashMap<>();
              tmpMap.putAll(map);
              String source = mapper.getSource();
              List<String> params = ConstructExpressionUtil.extractMapperParam(source);
              if (CollectionUtils.isNotEmpty(params)) {

                String value = String.format("${responseBody.%s}", params.get(0));
                String result =
                    JsonToolkit.generateJson(
                        convertToJsonPointer(
                            mapper
                                .getTarget()
                                .replace(RESPONSE_BODY, StringUtils.EMPTY)
                                .replace(rootPath + ".", StringUtils.EMPTY)),
                        value,
                        JsonToolkit.toJson(tmpMap));
                map.putAll(JsonToolkit.toMap(result));
              }
            });
    return map;
  }
}
