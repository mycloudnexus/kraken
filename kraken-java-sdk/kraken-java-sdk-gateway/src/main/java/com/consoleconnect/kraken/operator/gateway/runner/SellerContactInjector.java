package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.SellerContactFacets;
import com.consoleconnect.kraken.operator.core.service.ApiUseCaseSelector;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;

public interface SellerContactInjector extends ApiUseCaseSelector {

  UnifiedAssetService getUnifiedAssetService();

  default void inject(Map<String, Object> inputs, String targetKey) {
    Optional<ApiUseCaseDto> apiUseCaseDtoOptional = findRelatedApiUse(targetKey, findApiUseCase());
    if (apiUseCaseDtoOptional.isEmpty()) {
      return;
    }

    String sellerContactKey =
        generateSellerContactKey(
            apiUseCaseDtoOptional.get().getComponentApiKey(),
            ProductCategoryEnum.ACCESS_ELINE.getKind());
    List<String> sellerContactKeys = List.of(sellerContactKey);
    List<UnifiedAssetDto> sellerContactAssets =
        getUnifiedAssetService().findByAllKeysIn(sellerContactKeys, true);
    if (CollectionUtils.isEmpty(sellerContactAssets)) {
      return;
    }

    UnifiedAssetDto sellerContactAsset = sellerContactAssets.get(0);
    SellerContactFacets facets =
        UnifiedAsset.getFacets(sellerContactAsset, new TypeReference<SellerContactFacets>() {});
    SellerContactFacets.SellerInfo sellerInfo = facets.getSellerInfo();
    // Read configuration from application.yaml
    Map<String, Object> envMap =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(inputs.get("env")), new TypeReference<Map<String, Object>>() {});
    Map<String, Object> sellerMap =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(envMap.get("seller")), new TypeReference<Map<String, Object>>() {});
    // Overwrite the configurations of application.yaml with data from the database.
    sellerMap.put("name", sellerInfo.getContactName());
    sellerMap.put("number", sellerInfo.getContactPhone());
    sellerMap.put("emailAddress", sellerInfo.getContactEmail());
    envMap.put("seller", sellerMap);
    inputs.put("env", envMap);
  }

  default String generateSellerContactKey(String componentKey, String productCategoryKind) {
    return componentKey + Constants.DOT + productCategoryKind;
  }
}
