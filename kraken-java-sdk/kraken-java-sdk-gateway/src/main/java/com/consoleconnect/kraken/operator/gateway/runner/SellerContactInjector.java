package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.SellerContactFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

public interface SellerContactInjector {

  UnifiedAssetService getUnifiedAssetService();

  default void inject(Map<String, Object> inputs, ComponentAPITargetFacets.Trigger trigger) {
    List<String> sellerContactKeys = trigger.getSellerContactKeys();
    if (CollectionUtils.isEmpty(sellerContactKeys)) {
      return;
    }
    List<UnifiedAssetDto> sellerContactAssets =
        getUnifiedAssetService().findByAllKeysIn(sellerContactKeys, true);
    if (CollectionUtils.isEmpty(sellerContactAssets)) {
      return;
    }
    UnifiedAssetDto sellerContactAsset = sellerContactAssets.get(0);
    SellerContactFacets facets =
        UnifiedAsset.getFacets(sellerContactAsset, new TypeReference<SellerContactFacets>() {});
    SellerContactFacets.SellerInfo sellerInfo = facets.getSellerInfo();
    Map<String, Object> envMap =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(inputs.get("env")), new TypeReference<Map<String, Object>>() {});
    Map<String, Object> sellerMap =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(envMap.get("seller")), new TypeReference<Map<String, Object>>() {});
    sellerMap.put("name", sellerInfo.getContactName());
    sellerMap.put("number", sellerInfo.getContactPhone());
    sellerMap.put("emailAddress", sellerInfo.getContactEmail());
    envMap.put("seller", sellerMap);
    inputs.put("env", envMap);
  }
}
