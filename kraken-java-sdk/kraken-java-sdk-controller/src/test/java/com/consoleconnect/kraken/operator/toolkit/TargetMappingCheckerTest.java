package com.consoleconnect.kraken.operator.toolkit;

import com.consoleconnect.kraken.operator.controller.service.TargetMappingChecker;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import java.util.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TargetMappingCheckerTest extends AbstractIntegrationTest implements TargetMappingChecker {
  @SneakyThrows
  @Test
  void testQuoteResponseMapper() {
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(
            readFileToString(
                "deployment-config/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml"),
            UnifiedAsset.class);
    UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
    Assertions.assertDoesNotThrow(
        () -> {
          checkEndpoint(mapperFacets.getEndpoints().get(0), false);
        });
  }

  @SneakyThrows
  @Test
  void givenSourceAndTarget_whenCheckEndpoint_thenNoException() {
    ComponentAPITargetFacets.Mapper mapperItem = new ComponentAPITargetFacets.Mapper();
    mapperItem.setSource("123");
    mapperItem.setTarget("456");
    mapperItem.setRequiredMapping(true);

    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setRequest(List.of(mapperItem));
    mappers.setResponse(List.of(mapperItem));

    ComponentAPITargetFacets.Endpoint endpoint1 = new ComponentAPITargetFacets.Endpoint();
    endpoint1.setPath("/xxx");
    endpoint1.setMethod("get");
    endpoint1.setServerKey("mef.sonata.api-target-spec.con1718940696857");

    endpoint1.setMappers(mappers);
    Assertions.assertDoesNotThrow(
        () -> {
          checkEndpoint(endpoint1, false);
        });
    Assertions.assertDoesNotThrow(
        () -> {
          checkEndpoint(endpoint1, true);
        });
  }

  @SneakyThrows
  @Test
  void givenIncompleteMapping_whenCheckEndpoint_thenThrowException() {
    ComponentAPITargetFacets.Endpoint endpoint1 = new ComponentAPITargetFacets.Endpoint();
    Assertions.assertThrows(
        KrakenException.class,
        () -> {
          checkEndpoint(endpoint1, false);
        });
    endpoint1.setPath("/xxx");
    endpoint1.setMethod("get");
    Assertions.assertThrows(
        KrakenException.class,
        () -> {
          checkEndpoint(endpoint1, false);
        });

    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    endpoint1.setMappers(mappers);
    Assertions.assertThrows(
        KrakenException.class,
        () -> {
          checkEndpoint(endpoint1, false);
        });
    ComponentAPITargetFacets.Mapper mapperItem = new ComponentAPITargetFacets.Mapper();
    mapperItem.setTarget("");
    mappers.setResponse(List.of());
    endpoint1.setMappers(mappers);
    Assertions.assertThrows(
        KrakenException.class,
        () -> {
          checkEndpoint(endpoint1, false);
        });
    mapperItem.setTargetType("enum");
    mapperItem.setRequiredMapping(true);
    endpoint1.setServerKey("mef.sonata.api-target-spec.con1718940696857");

    mappers.setResponse(List.of(mapperItem));
    endpoint1.setMappers(mappers);
    Assertions.assertThrows(
        KrakenException.class,
        () -> {
          checkEndpoint(endpoint1, false);
        });
  }

  @Test
  void givenDefaultValueToSource_whenValidateResponse_thenOK() {
    Map<String, String> valueMapping = new HashMap<>();

    ComponentAPITargetFacets.Mapper mapper = new ComponentAPITargetFacets.Mapper();
    mapper.setSource("source_default_value");
    mapper.setTarget("target");
    mapper.setValueMapping(valueMapping);
    mapper.setTargetType("enum");
    mapper.setRequiredMapping(true);

    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setResponse(List.of(mapper));

    ComponentAPITargetFacets.Endpoint endpoint = new ComponentAPITargetFacets.Endpoint();
    endpoint.setPath("/xxx");
    endpoint.setMethod("get");
    endpoint.setServerKey("mef.sonata.api-target-spec.con1718940696857");
    endpoint.setMappers(mappers);
    Assertions.assertTrue(isResponseValid(endpoint));
  }

  @Test
  void givenVariableToSource_whenValidateResponse_thenOK() {
    Map<String, String> valueMapping = new HashMap<>();

    ComponentAPITargetFacets.Mapper mapper = new ComponentAPITargetFacets.Mapper();
    mapper.setSource("@{{source_default_value}}");
    mapper.setTarget("target");
    mapper.setValueMapping(valueMapping);
    mapper.setTargetType("enum");
    mapper.setRequiredMapping(true);

    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setResponse(List.of(mapper));

    ComponentAPITargetFacets.Endpoint endpoint = new ComponentAPITargetFacets.Endpoint();
    endpoint.setPath("/xxx");
    endpoint.setMethod("get");
    endpoint.setServerKey("mef.sonata.api-target-spec.con1718940696857");
    endpoint.setMappers(mappers);
    Assertions.assertFalse(isResponseValid(endpoint));
  }

  @Test
  void givenMapperKey_whenExtractTargetKey_thenOK() {
    Assertions.assertEquals("", extractTargetKey("   "));
    Assertions.assertEquals("", extractTargetKey("hello"));
    Assertions.assertEquals(
        "mef.sonata.api-target.quote.eline.add",
        extractTargetKey("mef.sonata.api-target-mapper.quote.eline.add"));
    Assertions.assertEquals(
        "mef.sonata.api-target-xxx", extractTargetKey("mef.sonata.api-target-xxx-mapper"));
  }

  @Test
  void givenKeywords_whenCheckContains_thenOK() {
    List<String> noRequiredMappingKeys = new ArrayList<>();
    noRequiredMappingKeys.add("mef.sonata.api-target-mapper.poq.eline.read");
    noRequiredMappingKeys.add("mef.sonata.api-target-mapper.poq.uni.read");
    noRequiredMappingKeys.add("mef.sonata.api-target-mapper.quote.eline.read.sync");
    noRequiredMappingKeys.add("mef.sonata.api-target-mapper.quote.uni.read.sync");
    Assertions.assertFalse(containsKeywords(List.of(), "123"));
    Assertions.assertFalse(containsKeywords(noRequiredMappingKeys, ""));
    Assertions.assertFalse(
        containsKeywords(noRequiredMappingKeys, "mef.sonata.api-target-mapper.quote.uni.read"));
    Assertions.assertTrue(
        containsKeywords(noRequiredMappingKeys, "mef.sonata.api-target-mapper.poq.uni.read"));
  }

  @SneakyThrows
  @Test
  void givenMapper_whenTestingEquals_thenTrue() {
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(
            readFileToString(
                "deployment-config/api-targets-mappers/api-target-mapper.order.eline.add.yaml"),
            UnifiedAsset.class);
    if (mapperAssetOpt.isPresent()) {
      UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
      ComponentAPITargetFacets newFacets =
          UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
      ComponentAPITargetFacets.Endpoint newEndpoints = newFacets.getEndpoints().get(0);
      ComponentAPITargetFacets.Mapper item1 = newEndpoints.getMappers().getRequest().get(0);
      ComponentAPITargetFacets.Mapper item2 = new ComponentAPITargetFacets.Mapper();
      item2.setName("order.eline.add.buyerId.mapper");
      item2.setSource("@{{buyerId}}");
      item2.setSourceLocation("QUERY");
      item2.setTarget("@{{companyName}}");
      item2.setTargetLocation("PATH");
      item2.setTargetType(null);
      item2.setTargetValues(null);
      item2.setValueMapping(null);
      boolean result = item1.equals(item2);
      Assertions.assertTrue(result);
    }
  }
}
