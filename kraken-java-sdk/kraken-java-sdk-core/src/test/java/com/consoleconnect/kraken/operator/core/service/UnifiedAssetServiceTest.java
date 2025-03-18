package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class UnifiedAssetServiceTest extends AbstractIntegrationTest {

  @Autowired private UnifiedAssetService unifiedAssetService;

  private static final String MAPPER_REQUEST = "request";

  private static final String MAPPER_RESPONSE = "response";

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
    assertThat(assetDtoPaging.getData(), hasSize(0));
  }

  @Test
  void givenNewMapper_whenEnforce_thenUpdateSuccess() {
    List<UnifiedAssetDto> list =
        unifiedAssetService.findByKind(COMPONENT_API_TARGET_SPEC.getKind());
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
    String s = readFileToString("data/mapper_old.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> existMapperMap =
        JsonToolkit.fromJson(
            s, new TypeReference<Map<String, Map<String, ComponentAPITargetFacets.Mapper>>>() {});
    int beforeSize = existMapperMap.size();
    unifiedAssetService.mergeMappers(existMapperMap, existMapperMap);
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
    String s1 = readFileToString("data/mapper_old.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> existMapperMap =
        JsonToolkit.fromJson(s1, new TypeReference<>() {});
    String s2 = readFileToString("data/mapper_new.json");
    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> newMapperMap =
        JsonToolkit.fromJson(s2, new TypeReference<>() {});
    String s3 = readFileToString("data/mapper_merged.json");

    Map<String, Map<String, ComponentAPITargetFacets.Mapper>> expectedResults =
        JsonToolkit.fromJson(s3, new TypeReference<>() {});

    unifiedAssetService.mergeMappers(existMapperMap, newMapperMap);
    String existMapperStr = JsonToolkit.toJson(existMapperMap);
    log.info(existMapperStr);
    Assertions.assertNotNull(existMapperStr);
    String newMapperStr = JsonToolkit.toJson(newMapperMap);
    log.info(newMapperStr);
    Assertions.assertNotNull(newMapperStr);

    /*
     * mapper.testcase01.updateSystemMapping
     * | property                 | old                          | new                          | merged                       |
     * | request.customizedField  | false                        | false                        | false                        |
     * | request.allowValueLimit  | true                         | false                        | false                        |
     * | request.target           | @{{speed-old}}               | @{{speed-new}}               | @{{speed-old}}               |
     * | response.customizedField | false                        | false                        | false                        |
     * | response.allowValueLimit | true                         | false                        | false                        |
     * | response.source          | @{{responseBody.status-old}} | @{{responseBody.status-new}} | @{{responseBody.status-old}} |
     */
    verifyMergedMapper("mapper.testcase01.updateSystemMapping", newMapperMap, expectedResults);

    /*
     * mapper.testcase02.addNewSystemMapping
     * | property                 | old                          | new                          | merged                       |
     * | request.customizedField  | N/A                          | false                        | false                        |
     * | request.allowValueLimit  | N/A                          | false                        | false                        |
     * | request.target           | N/A                          | @{{speed-new}}               | @{{speed-new}}               |
     * | response.customizedField | N/A                          | false                        | false                        |
     * | response.allowValueLimit | N/A                          | false                        | false                        |
     * | response.source          | N/A                          | @{{responseBody.status-new}} | @{{responseBody.status-new}} |
     */
    verifyMergedMapper("mapper.testcase02.addNewSystemMapping", newMapperMap, expectedResults);

    /*
     * mapper.testcase03.changeSystemMappingToDeleteToCustomized
     * requiredMapping(false) --merge --> requiredMapping(false)
     * | property                 | old                          | new   | merged                       |
     * | request.customizedField  | false                        | N/A   | true                         |
     * | request.requiredMapping  | false                        | N/A   | false                        |
     * | request.allowValueLimit  | true                         | N/A   | false                        |
     * | request.target           | @{{speed-old}}               | N/A   | @{{speed-old}}               |
     * | response.customizedField | false                        | N/A   | true                         |
     * | response.requiredMapping | false                        | N/A   | false                       |
     * | response.allowValueLimit | true                         | N/A   | false                        |
     * | response.source          | @{{responseBody.status-old}} | N/A   | @{{responseBody.status-old}} |
     */
    verifyMergedMapper(
        "mapper.testcase03.changeSystemMappingToDeleteToCustomized", newMapperMap, expectedResults);

    /*
     * mapper.testcase03.changeSystemMappingToDeleteToCustomized2
     * requiredMapping(true) --merge --> requiredMapping(false)
     * | property                 | old                          | new   | merged                       |
     * | request.customizedField  | false                        | N/A   | true                         |
     * | request.requiredMapping  | true                         | N/A   | false                        |
     * | request.allowValueLimit  | true                         | N/A   | false                        |
     * | request.target           | @{{speed-old}}               | N/A   | @{{speed-old}}               |
     * | response.customizedField | false                        | N/A   | true                         |
     * | response.requiredMapping | true                         | N/A   | false                        |
     * | response.allowValueLimit | true                         | N/A   | false                        |
     * | response.source          | @{{responseBody.status-old}} | N/A   | @{{responseBody.status-old}} |
     */
    verifyMergedMapper(
        "mapper.testcase03.changeSystemMappingToDeleteToCustomized2",
        newMapperMap,
        expectedResults);

    /*
     * mapper.testcase04.keepCustomizedMappingToDeleteConfiguredAndNoConflict
     * | property                 | old                          | new                        | merged                       |
     * | request.customizedField  | true                         | N/A                        | true                         |
     * | request.allowValueLimit  | true                         | N/A                        | false                        |
     * | request.target           | @{{speed-old}}               | N/A                        | @{{speed-old}}               |
     * | response.customizedField | true                         | N/A                        | true                         |
     * | response.allowValueLimit | true                         | N/A                        | false                        |
     * | response.source          | @{{responseBody.status-old}} | N/A                        | @{{responseBody.status-old}} |
     */
    verifyMergedMapper(
        "mapper.testcase04.keepCustomizedMappingToDeleteConfiguredAndNoConflict",
        newMapperMap,
        expectedResults);

    /*
     * mapper.testcase05.deleteOldMappingNotConfigured
     * | property                 | old                          | new                         | merged    |
     * | request.customizedField  | false                        | N/A                         | N/A       |
     * | request.allowValueLimit  | true                         | N/A                         | N/A       |
     * | request.target           | NULL                         | N/A                         | N/A       |
     * | response.customizedField | false                        | N/A                         | N/A       |
     * | response.allowValueLimit | true                         | N/A                         | N/A       |
     * | response.source          | NULL                         | N/A                         | N/A       |
     */
    verifyDeletedMapper("mapper.testcase05.deleteOldMappingNotConfigured", newMapperMap);

    /*
     * mapper.testcase06.mergeSystemToCustomizedMapping
     * | property                 | old                          | new                          | merged                       |
     * | request.customizedField  | true                         | false                        | false                        |
     * | request.allowValueLimit  | true                         | false                        | false                        |
     * | request.target           | @{{speed-old}}               | @{{speed-new}}               | @{{speed-old}}               |
     * | response.customizedField | true                         | false                        | false                        |
     * | response.allowValueLimit | true                         | false                        | false                        |
     * | response.source          | @{{responseBody.status-old}} | @{{responseBody.status-new}} | @{{responseBody.status-old}} |
     */
    verifyMergedMapper(
        "mapper.testcase06.mergeSystemToCustomizedMapping", newMapperMap, expectedResults);
  }

  private void verifyDeletedMapper(
      String testcase, Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mergedMapperMap) {
    verifyDeletedMapper(mergedMapperMap, testcase, MAPPER_REQUEST);
    verifyDeletedMapper(mergedMapperMap, testcase, MAPPER_RESPONSE);
  }

  private void verifyDeletedMapper(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mergedMapperMap,
      String name,
      String mapperSection) {
    String fullName = name + "." + mapperSection;
    Assertions.assertFalse(mergedMapperMap.containsKey(fullName));
  }

  private void verifyMergedMapper(
      String testcase,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mergedMapperMap,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> expectedMapperMap) {
    verifyMergedMapper(mergedMapperMap, expectedMapperMap, testcase, MAPPER_REQUEST);
    verifyMergedMapper(mergedMapperMap, expectedMapperMap, testcase, MAPPER_RESPONSE);
  }

  private void verifyMergedMapper(
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> mergedMapperMap,
      Map<String, Map<String, ComponentAPITargetFacets.Mapper>> expectedMapperMap,
      String name,
      String mapperSection) {
    String fullName = name + "." + mapperSection;
    ComponentAPITargetFacets.Mapper mergedMapper = mergedMapperMap.get(fullName).get(mapperSection);
    ComponentAPITargetFacets.Mapper expectedMapper =
        expectedMapperMap.get(fullName).get(mapperSection);
    Assertions.assertEquals(expectedMapper.getTitle(), mergedMapper.getTitle());
    Assertions.assertEquals(expectedMapper.getName(), mergedMapper.getName());
    Assertions.assertEquals(expectedMapper.getDescription(), mergedMapper.getDescription());
    Assertions.assertEquals(expectedMapper.getSource(), mergedMapper.getSource());
    Assertions.assertEquals(expectedMapper.getSourceType(), mergedMapper.getSourceType());
    Assertions.assertEquals(expectedMapper.getSourceLocation(), mergedMapper.getSourceLocation());
    Assertions.assertEquals(
        expectedMapper.getSourceConditionExpression(), mergedMapper.getSourceConditionExpression());
    Assertions.assertEquals(
        expectedMapper.getSourceConditions(), mergedMapper.getSourceConditions());
    Assertions.assertEquals(expectedMapper.getAllowValueLimit(), mergedMapper.getAllowValueLimit());
    Assertions.assertEquals(expectedMapper.getDiscrete(), mergedMapper.getDiscrete());
    Assertions.assertEquals(expectedMapper.getSourceValues(), mergedMapper.getSourceValues());
    Assertions.assertEquals(expectedMapper.getTarget(), mergedMapper.getTarget());
    Assertions.assertEquals(expectedMapper.getTargetType(), mergedMapper.getTargetType());
    Assertions.assertEquals(expectedMapper.getTargetLocation(), mergedMapper.getTargetLocation());
    Assertions.assertEquals(expectedMapper.getRequiredMapping(), mergedMapper.getRequiredMapping());
    Assertions.assertEquals(expectedMapper.getReplaceStar(), mergedMapper.getReplaceStar());
    Assertions.assertEquals(expectedMapper.getDefaultValue(), mergedMapper.getDefaultValue());
    Assertions.assertEquals(expectedMapper.getTargetValues(), mergedMapper.getTargetValues());
    Assertions.assertEquals(expectedMapper.getValueMapping(), mergedMapper.getValueMapping());
    Assertions.assertEquals(expectedMapper.getFunction(), mergedMapper.getFunction());
    Assertions.assertEquals(expectedMapper.getCheckPath(), mergedMapper.getCheckPath());
    Assertions.assertEquals(expectedMapper.getDeletePath(), mergedMapper.getDeletePath());
    Assertions.assertEquals(expectedMapper.getCustomizedField(), mergedMapper.getCustomizedField());
    Assertions.assertEquals(expectedMapper.getConvertValue(), mergedMapper.getConvertValue());
  }

  @SneakyThrows
  @Test
  void givenQuoteRules_whenCopy_thenReturnOK() {
    String targetApiPath1 = "data/target-mapper.quote.uni.add.sync-1.yaml";
    Optional<UnifiedAsset> unifiedAssetOptOld =
        YamlToolkit.parseYaml(readFileToString(targetApiPath1), UnifiedAsset.class);
    String targetApiPath2 = "data/target-mapper.quote.uni.add.sync-2.yaml";
    Optional<UnifiedAsset> unifiedAssetOptNew =
        YamlToolkit.parseYaml(readFileToString(targetApiPath2), UnifiedAsset.class);
    if (unifiedAssetOptOld.isPresent() && unifiedAssetOptNew.isPresent()) {
      UnifiedAsset assetOld = unifiedAssetOptOld.get();
      UnifiedAsset assetNew = unifiedAssetOptNew.get();

      ComponentAPITargetFacets facetsOld =
          UnifiedAsset.getFacets(assetOld, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets facetsNew =
          UnifiedAsset.getFacets(assetNew, ComponentAPITargetFacets.class);

      String result1 = JsonToolkit.toJson(facetsOld);
      Assertions.assertNotNull(result1);
      Map<String, Object> map = unifiedAssetService.mergeFacets(facetsOld, facetsNew);
      String result2 = JsonToolkit.toJson(map);
      Assertions.assertNotNull(result2);
      assertThat(result2, hasJsonPath("$.endpoints[0].mappers.request", hasSize(9)));
      assertThat(result2, hasJsonPath("$.endpoints[0].mappers.response", hasSize(10)));
      assertThat(result2, hasJsonPath("$.endpoints[0].mappers.pathRules", hasSize(1)));
      assertThat(
          result2, hasJsonPath("$.endpoints[0].mappers.request[1].sourceValues", notNullValue()));
      assertThat(result2, hasJsonPath("$.endpoints[0].mappers.response[0].target", notNullValue()));
      assertThat(
          result2, hasJsonPath("$.endpoints[0].mappers.pathRules[0].checkPath", notNullValue()));
    }
  }

  @SneakyThrows
  @Test
  void givenCommonSchema_whenMergeMappers_thenReturnOK() {
    String targetApiPath1 = "data/api-target-mapper.inventory.uni.list-1.yaml";
    Optional<UnifiedAsset> unifiedAssetOptOld =
        YamlToolkit.parseYaml(readFileToString(targetApiPath1), UnifiedAsset.class);
    String targetApiPath2 = "data/api-target-mapper.inventory.uni.list-2.yaml";
    Optional<UnifiedAsset> unifiedAssetOptNew =
        YamlToolkit.parseYaml(readFileToString(targetApiPath2), UnifiedAsset.class);
    if (unifiedAssetOptOld.isPresent() && unifiedAssetOptNew.isPresent()) {
      UnifiedAsset assetOld = unifiedAssetOptOld.get();
      UnifiedAsset assetNew = unifiedAssetOptNew.get();

      ComponentAPITargetFacets facetsOld =
          UnifiedAsset.getFacets(assetOld, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets facetsNew =
          UnifiedAsset.getFacets(assetNew, ComponentAPITargetFacets.class);

      String result1 = JsonToolkit.toJson(facetsOld);
      Assertions.assertNotNull(result1);
      Map<String, Object> map = unifiedAssetService.mergeFacets(facetsOld, facetsNew);
      String result2 = JsonToolkit.toJson(map);
      Assertions.assertNotNull(result2);
      Assertions.assertEquals(result1.hashCode(), result2.hashCode());
    }
  }

  @SneakyThrows
  @Test
  void givenBlankClassPath_whenReading_thenReturnEmpty() {
    String path = "classpath:/data/api-target-mapper.inventory.common.list-1.yaml";
    Optional<UnifiedAsset> opt = unifiedAssetService.readFromPath(path);
    Assertions.assertTrue(opt.isEmpty());
  }
}
