package com.consoleconnect.kraken.operator.core.service;

import static org.testcontainers.shaded.org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.dto.SimpleApiServerDto;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class UnifiedAssetServiceTest extends AbstractIntegrationTest {

  @SpyBean private UnifiedAssetService unifiedAssetService;

  @Order(10)
  @Test
  void givenNotExistingChildren_whenRemove_thenResultNoException() {
    Assertions.assertDoesNotThrow(
        () -> {
          UnifiedAssetDto assetDto = unifiedAssetService.findOne("mef.sonata.api-spec.order");
          unifiedAssetService.removeNotExistingChildren(assetDto.getParentId());
        });
  }

  @SneakyThrows
  @Order(20)
  @Test
  void givenAsset_whenDelete_thenResultNoException() {
    Assertions.assertDoesNotThrow(
        () -> {
          unifiedAssetService.deleteOne("test.add.component");
        });
  }

  @Test
  void givenPageSize_whenSearch_thenPageSizeIsOK() {
    PageRequest page1 = UnifiedAssetService.getSearchPageRequest();
    PageRequest page2 = UnifiedAssetService.getSearchPageRequest(0, 10);
    Assertions.assertNotNull(page1);
    Assertions.assertEquals(20, page1.getPageSize());
    Assertions.assertNotNull(page2);
    Assertions.assertEquals(10, page2.getPageSize());
  }

  @SneakyThrows
  @Test
  void givenServerUrl_whenInject_thenOK() {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            AssetKindEnum.COMPONENT_API_SERVER.getKind(),
            AssetKindEnum.COMPONENT_API_SERVER.getKind(),
            null);
    Map<String, Object> facets = new HashMap<>();
    String serverUrls = readFileToString("/data/server_url.json");
    List<SimpleApiServerDto> serverDtoList =
        JsonToolkit.fromJson(
            StringUtils.compact(serverUrls), new TypeReference<List<SimpleApiServerDto>>() {});
    facets.put("urls", serverDtoList);
    unifiedAsset.setFacets(facets);
    SyncMetadata syncMetadata = new SyncMetadata();
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(null, unifiedAsset, syncMetadata, true);
    if (ingestionDataResult.getCode() != HttpStatus.OK.value()) {
      return;
    }
    UnifiedAssetEntity ingestedAsset = ingestionDataResult.getData();
    Assertions.assertNotNull(ingestedAsset);
  }

  @Test
  void testFindBySpecification() {
    List<Tuple2> tuple2List =
        Tuple2.ofList(
            AssetsConstants.FIELD_STATUS, AssetStatusEnum.ACTIVATED.getKind(),
            AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_BUYER.getKind());

    List<Tuple2> tuple3List = Tuple2.ofList(LabelConstants.LABEL_BUYER_ID, "buyer01");

    List<String> tags = Arrays.asList("12344", "456");

    PageRequest pageRequest =
        PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT);
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(tuple2List, tuple3List, tags, pageRequest, null);
    MatcherAssert.assertThat(assetDtoPaging.getData(), hasSize(0));
  }

  @Test
  void givenNewMapper_whenEnforce_thenUpdateSuccess() {
    List<UnifiedAssetDto> list = unifiedAssetService.findByKind(AssetsConstants.SERVER_KIND);
    Assertions.assertEquals(0, list.size());
    UnifiedAssetDto data =
        unifiedAssetService.findOne("mef.sonata.api-target-mapper.order.eline.add");
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(data.getParentId(), data, new SyncMetadata(), true);
    Assertions.assertEquals(200, ingestionDataResult.getCode());
  }

  @SneakyThrows
  @Test
  void givenMapper_whenTestingEquals_thenOK() {
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(
            readFileToString(
                "deployment-config/components/api-targets-mappers/api-target-mapper.order.eline.add.yaml"),
            UnifiedAsset.class);
    if (mapperAssetOpt.isPresent()) {
      UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
      ComponentAPITargetFacets newFacets =
          UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint newEndpoints = newFacets.getEndpoints().get(0);
      ComponentAPITargetFacets.Mapper item1 = newEndpoints.getMappers().getResponse().get(0);
      ComponentAPITargetFacets.Mapper item2 = new ComponentAPITargetFacets.Mapper();
      item2.setName("mapper.order.eline.add.state");
      item2.setSource("@{{responseBody.status}}");
      item2.setSourceLocation("BODY");
      item2.setTarget("@{{status}}");
      item2.setTargetLocation("BODY");
      item2.setTargetType("enum");
      item2.setTargetValues(List.of("acknowledged"));
      item2.setValueMapping(Map.of(" CREATING", "acknowledged"));
      boolean result = item1.equals(item2);
      Assertions.assertFalse(result);
    }
  }

  @SneakyThrows
  @Test
  void givenTwoMappers_whenMerge_thenRemoveDuplicated() {
    String s = readFileToString("data/mapper_1.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> existMapperMap =
        JsonToolkit.fromJson(
            s, new TypeReference<Map<String, Map<String, ComponentAPITargetFacets.Mapper>>>() {});
    int beforeSize = existMapperMap.size();
    UnifiedAssetService.mergeMappers(existMapperMap, existMapperMap);
    int afterSize = existMapperMap.size();
    Assertions.assertEquals(beforeSize, afterSize);
  }

  @Test
  void givenNotExistedUuidOrKey_whenExisted_thenReturnFalse() {
    UUID uuid = UUID.randomUUID();
    Assertions.assertFalse(unifiedAssetService.existed(uuid.toString()));
    Assertions.assertFalse(unifiedAssetService.existed("mef.sonata.test"));
  }

  @SneakyThrows
  @Test
  void givenSourceValues_whenMerge_thenReturnOK() {
    String s1 = readFileToString("data/mapper_1.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> existMapperMap =
        JsonToolkit.fromJson(s1, new TypeReference<>() {});
    String s2 = readFileToString("data/mapper_2.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> newMapperMap =
        JsonToolkit.fromJson(s2, new TypeReference<>() {});
    UnifiedAssetService.mergeMappers(existMapperMap, newMapperMap);
    String existMapperStr = JsonToolkit.toJson(existMapperMap);
    log.info(existMapperStr);
    Assertions.assertNotNull(existMapperStr);
    String newMapperStr = JsonToolkit.toJson(newMapperMap);
    log.info(newMapperStr);
    Assertions.assertNotNull(newMapperStr);
  }
}
