package com.consoleconnect.kraken.operator.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.controller.dto.CreateAPIServerRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.APISpecContentFormatEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

public interface APIServerCreator {

  String PRODUCT_BASE_PATH = "/v2/products";
  String PRODUCT_ID = "product.mef.sonata.api";
  String COMPONENT_ID = "mef.sonata.api.order";

  WebTestClientHelper getTestClientHelper();

  default void createAPIServer(String productID, String componentId) {
    String key = "mef.sonata.api-target-spec.tes" + System.currentTimeMillis();
    createAPIServer(productID, componentId, key);
  }

  default void createAPIServer(String productID, String componentId, String key) {
    String path =
        String.format("%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, productID, componentId);

    CreateAPIServerRequest request = new CreateAPIServerRequest();

    request.setName("this is a readable name");
    request.setDescription("this is a short description");
    request.setKey(key);

    ComponentAPISpecFacets.APISpec apiSpec = new ComponentAPISpecFacets.APISpec();
    apiSpec.setContent(Base64.encodeBase64String("this is a open api spec".getBytes()));
    apiSpec.setPath("https://test.com");
    apiSpec.setFormat(APISpecContentFormatEnum.OPEN_API);

    request.setBaseSpec(apiSpec);

    request.setSelectedAPIs(List.of("/api/v1/xxx get"));

    Map<String, String> environments = new HashMap<>();
    environments.put("stage", "url1");
    environments.put("production", "url2");
    request.setEnvironments(environments);
    getTestClientHelper()
        .postAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            JsonToolkit.toJson(request),
            bodyStr -> {
              assertThat(bodyStr, hasJsonPath("$.data", notNullValue()));
            });
  }

  default List<UnifiedAssetDto> queryAPIServerList(String path) {
    List<UnifiedAssetDto> assetDtoList = new ArrayList<>();
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            bodyStr -> {
              assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
              HttpResponse<Paging<UnifiedAssetDto>> assetDtoPages =
                  JsonToolkit.fromJson(
                      bodyStr, new TypeReference<HttpResponse<Paging<UnifiedAssetDto>>>() {});
              UnifiedAssetDto assetDto = assetDtoPages.getData().getData().get(0);
              assetDtoList.add(assetDto);
            });
    return assetDtoList;
  }

  default List<UnifiedAssetDto> queryAPIServerList(
      String path, boolean facetIncluded, boolean liteSearch) {
    List<UnifiedAssetDto> assetDtoList = new ArrayList<>();
    getTestClientHelper()
        .getAndVerify(
            (uriBuilder ->
                uriBuilder
                    .path(path)
                    .queryParam("facetIncluded", facetIncluded)
                    .queryParam("liteSearch", liteSearch)
                    .build()),
            bodyStr -> {
              assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
              HttpResponse<Paging<UnifiedAssetDto>> assetDtoPages =
                  JsonToolkit.fromJson(
                      bodyStr, new TypeReference<HttpResponse<Paging<UnifiedAssetDto>>>() {});
              UnifiedAssetDto assetDto = assetDtoPages.getData().getData().get(0);
              assetDtoList.add(assetDto);
            });
    return assetDtoList;
  }
}
