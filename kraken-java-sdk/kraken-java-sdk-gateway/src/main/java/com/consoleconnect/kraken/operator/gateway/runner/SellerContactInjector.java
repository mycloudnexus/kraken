package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.ENV;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
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
  String SELLER_KEY_WORD = "seller";

  UnifiedAssetService getUnifiedAssetService();

  default void inject(Map<String, Object> inputs, String targetKey) {
    Optional<ApiUseCaseDto> apiUseCaseDtoOptional = findRelatedApiUse(targetKey, findApiUseCase());
    if (apiUseCaseDtoOptional.isEmpty()) {
      return;
    }

    String sellerContactKey =
        generateSellerContactKey(
            apiUseCaseDtoOptional.get().getComponentApiKey(),
            ParentProductTypeEnum.ACCESS_ELINE.getKind());
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
            JsonToolkit.toJson(inputs.get(ENV)), new TypeReference<Map<String, Object>>() {});
    SellerContactFacets.SellerInfo currentSeller = new SellerContactFacets.SellerInfo();
    // Overwrite the configurations of application.yaml with data from the database.
    currentSeller.setName(sellerInfo.getName());
    currentSeller.setNumber(sellerInfo.getNumber());
    currentSeller.setEmailAddress(sellerInfo.getEmailAddress());
    currentSeller.setRole(sellerInfo.getRole());
    envMap.put(SELLER_KEY_WORD, currentSeller);
    inputs.put(ENV, envMap);
  }

  default String generateSellerContactKey(String componentKey, String productCategoryKind) {
    return componentKey + Constants.DOT + productCategoryKind;
  }
}
