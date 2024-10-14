package com.consoleconnect.kraken.operator;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.MAPPER_SIGN;

import com.consoleconnect.kraken.operator.core.dto.ResponseTargetMapperDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class ResponseMappingTest extends AbstractIntegrationTest implements MappingTransformer {

  @SneakyThrows
  @Test
  void testResponseMapper() {
    String expected1 =
        "{\"validationResult\":\"((mefResponseBody.bestMatchGeographicAddress.id==''?(mefResponseBody.alternateGeographicAddress.size()==0?'fail':'partial'):'success'))\",\"alternateGeographicAddress\":[{\"id\":\"${responseBody[*].id}\",\"@type\":\"FieldedAddress\",\"country\":\"${responseBody[*].company.addresses[0].country}\",\"city\":\"${responseBody[*].company.addresses[0].city}\",\"streetName\":\"${responseBody[*].company.addresses[0].address}\"}],\"provideAlternative\":\"${mefRequestBody.provideAlternative}\",\"submittedGeographicAddress\":\"${mefRequestBody.submittedGeographicAddress}\",\"bestMatchGeographicAddress\":{\"id\":\"123\",\"type\":\"FieldedAddress\",\"city\":\"Sydney\",\"country\":\"Sydney\"}}";
    String input =
        getTarget(
            "/mock/api-targets/api-target.address.validate.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.validate.yaml");
    validate(expected1, input);

    String expected2 =
        "{\"id\":\"${responseBody.id}\",\"type\":\"FieldedAddress\",\"associatedGeographicAddress\":{\"country\":\"${responseBody.company.addresses[0].country}\",\"city\":\"${responseBody.company.addresses[0].city}\",\"streetName\":\"${responseBody.company.addresses[0].address}\"}}";
    String input2 =
        getTarget(
            "/mock/api-targets/api-target.address.retrieve.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.retrieve.yaml");
    validate(expected2, input2);

    String expectedOrder =
        "{\"relatedContactInformation\":\"${mefRequestBody.relatedContactInformation}\",\"id\":\"${entity.id}\",\"orderDate\":\"function.now()\",\"productOrderItem\":\"${mefRequestBody.productOrderItem}\",\"state\":\"${responseBody.status}\"}";
    String input3 =
        getTarget(
            "/mock/api-targets/api-target.order.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.eline.add.yaml");

    validate(expectedOrder, input3);
    String input4 =
        getTarget(
            "/mock/api-targets/api-target.order.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.uni.add.yaml");
    validate(expectedOrder, input4);

    String expectedQuote =
        "{\"id\":\"${entity.id}\",\"buyerRequestedQuoteLevel\":\"${mefRequestBody.buyerRequestedQuoteLevel}\",\"quoteItem\":[{\"product\":\"${mefRequestBody.quoteItem[0].product}\",\"state\":\"answered\",\"quoteItemPrice\":{\"price\":{\"dutyFreeAmount\":{\"unit\":\"USD\",\"value\":\"${responseBody.results[0].price}\"}}}}],\"quoteDate\":\"function.now()\",\"externalId\":\"${mefRequestBody[externalId]?:''}\",\"instantSyncQuote\":\"${mefRequestBody[instantSyncQuote]?:''}\",\"requestedQuoteCompletionDate\":\"${mefRequestBody[requestedQuoteCompletionDate]?:''}\"}";
    String input5 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.yaml");
    validate(expectedQuote, input5);

    String input6 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.yaml");
    validate(expectedQuote, input6);

    String expected7 =
        "{\"id\":\"${entity.id}\",\"buyerRequestedQuoteLevel\":\"${entity.request.buyerRequestedQuoteLevel}\",\"quoteItem\":[{\"product\":\"${entity.request.quoteItem[0].product}\",\"state\":\"answered\",\"quoteItemPrice\":{\"price\":{\"dutyFreeAmount\":{\"unit\":\"USD\",\"value\":\"${responseBody.results[0].price}\"}}}}],\"quoteDate\":\"function.now()\",\"externalId\":\"${entity.request[externalId]?:''}\",\"instantSyncQuote\":\"${entity.request[instantSyncQuote]?:''}\",\"requestedQuoteCompletionDate\":\"${entity.request[requestedQuoteCompletionDate]?:''}\"}";
    String input7 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.yaml");
    validate(expected7, input7);

    String input8 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.yaml");
    validate(expected7, input8);

    String expected9 =
        "{\"id\":\"${entity.id}\",\"buyerRequestedQuoteLevel\":\"${mefRequestBody.buyerRequestedQuoteLevel}\",\"quoteItem\":[{\"product\":\"${mefRequestBody.quoteItem[0].product}\",\"action\":\"add\",\"id\":\"${mefRequestBody.quoteItem[0].id}\",\"state\":\"((mefResponseBody.quoteItem[0].quoteItemPrice[0].price.dutyFreeAmount.value==''?'unableToProvide':'approved.orderable'))\",\"quoteItemPrice\":[{\"unitOfMeasure\":\"Gb\",\"price\":{\"dutyFreeAmount\":{\"unit\":\"USD\",\"value\":\"${responseBody.results[0].price}\"},\"taxRate\":\"16\",\"taxIncludedAmount\":{\"unit\":\"USD\",\"value\":\"100\"}},\"name\":\"name-here\",\"priceType\":\"recurring\",\"description\":\"\",\"recurringChargePeriod\":\"month\"}]}],\"quoteDate\":\"now\",\"externalId\":\"${mefRequestBody[externalId]?:''}\",\"instantSyncQuote\":\"${mefRequestBody[instantSyncQuote]?:''}\",\"requestedQuoteCompletionDate\":\"${mefRequestBody[requestedQuoteCompletionDate]?:''}\"}";
    String input9 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml");
    validate(expected9, input9);

    String expected10 =
        "{\"id\":\"${entity.id}\",\"buyerRequestedQuoteLevel\":\"${entity.request.buyerRequestedQuoteLevel}\",\"quoteItem\":[{\"product\":\"${entity.request.quoteItem[0].product}\",\"action\":\"${entity.request.quoteItem[0].action}\",\"id\":\"${entity.request.quoteItem[0].id}\",\"state\":\"${entity.renderedResponse.quoteItem[0].state}\",\"quoteItemPrice\":\"${entity.renderedResponse.quoteItem[0].quoteItemPrice}\"}],\"quoteDate\":\"now\",\"externalId\":\"${entity.request[externalId]?:''}\",\"instantSyncQuote\":\"${entity.request[instantSyncQuote]?:''}\",\"requestedQuoteCompletionDate\":\"${entity.request[requestedQuoteCompletionDate]?:''}\"}";
    String input10 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.sync.yaml");
    validate(expected10, input10);

    String input11 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.sync.yaml");
    validate(expected9, input11);

    String input12 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.sync.yaml");
    validate(expected10, input12);
  }

  public void validate(String expected, String input) {
    UnifiedAsset asset = YamlToolkit.parseYaml(input, UnifiedAsset.class).get();
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    Assertions.assertNotNull(facets);

    ResponseTargetMapperDto responseTargetMapperDto = new ResponseTargetMapperDto();
    for (ComponentAPITargetFacets.Endpoint endpoint : facets.getEndpoints()) {
      String transformedResp = transform(endpoint, responseTargetMapperDto);
      log.info("expected1111111:{}", expected);
      log.info("transformedResp:{}", transformedResp);
      Assertions.assertEquals(expected, transformedResp);
    }
  }

  public String getTarget(String targetApiPath, String mapperApiPath) throws IOException {
    Optional<UnifiedAsset> unifiedAsset =
        YamlToolkit.parseYaml(readFileToString(targetApiPath), UnifiedAsset.class);
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(readFileToString(mapperApiPath), UnifiedAsset.class);

    UnifiedAsset targetAsset = unifiedAsset.get();
    UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
    String targetKey = extractTargetKey(targetMapperAsset.getMetadata().getKey());
    Assertions.assertEquals(targetAsset.getMetadata().getKey(), targetKey);

    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(targetAsset, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
    facets.getEndpoints().get(0).setPath(mapperFacets.getEndpoints().get(0).getPath());
    facets.getEndpoints().get(0).setMethod(mapperFacets.getEndpoints().get(0).getMethod());
    facets.getEndpoints().get(0).setMappers(mapperFacets.getEndpoints().get(0).getMappers());
    targetAsset.setFacets(JsonToolkit.fromJson(JsonToolkit.toJson(facets), Map.class));
    return JsonToolkit.toJson(targetAsset);
  }

  public String extractTargetKey(String targetMapperKey) {
    if (StringUtils.isBlank(targetMapperKey)) {
      return "";
    }
    int loc = targetMapperKey.indexOf(MAPPER_SIGN);
    if (loc < 0) {
      return "";
    }
    if (loc + MAPPER_SIGN.length() == targetMapperKey.length()) {
      return targetMapperKey.substring(0, loc);
    } else {
      return targetMapperKey.substring(0, loc)
          + targetMapperKey.substring(loc + MAPPER_SIGN.length());
    }
  }
}
