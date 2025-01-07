package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_SELLER_CONTACT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.dto.UpdateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.SellerContactFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Getter
@AllArgsConstructor
@Service
@Slf4j
public class SellerContactService {
  private static final String SELLER_CONTACT_PREFIX = "mef.sonata.seller.contact";
  private static final String SELLER_CONTACT_DESC = "seller contact information";
  private static final String COMPONENT_KEY = "componentKey";
  private static final String QUOTE_ROLE = "sellerContactInformation";
  private static final String ORDER_ROLE = "sellerContact";
  private final UnifiedAssetService unifiedAssetService;
  @Getter private final UnifiedAssetRepository unifiedAssetRepository;

  @Transactional
  public List<IngestionDataResult> createSellerContact(
      String productId, String componentId, CreateSellerContactRequest request, String createdBy) {
    UnifiedAssetDto componentAssetDto = unifiedAssetService.findOne(componentId);
    List<Pair<String, String>> keyList =
        generateKey(componentAssetDto.getMetadata().getKey(), request.getProductCategories());
    List<IngestionDataResult> results = new ArrayList<>();
    keyList.forEach(
        pair -> {
          UnifiedAsset sellerAsset =
              createSellerContact(request, pair.getLeft(), pair.getRight(), componentAssetDto);
          SyncMetadata syncMetadata =
              new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
          IngestionDataResult ingestionDataResult =
              unifiedAssetService.syncAsset(productId, sellerAsset, syncMetadata, true);
          results.add(ingestionDataResult);
        });
    return results;
  }

  private List<Pair<String, String>> generateKey(
      String componentKey, List<String> productCategories) {
    if (CollectionUtils.isEmpty(productCategories)) {
      productCategories =
          Arrays.stream(ProductCategoryEnum.values()).map(ProductCategoryEnum::getKind).toList();
    }
    return productCategories.stream()
        .map(item -> Pair.of(componentKey + DOT + item, item))
        .toList();
  }

  private String whichRole(UnifiedAssetDto componentAssetDto) {
    String key = componentAssetDto.getMetadata().getKey();
    if (key.contains(QUOTE_KEY_WORD)) {
      return QUOTE_ROLE;
    } else if (key.contains(ORDER_KEY_WORD)) {
      return ORDER_ROLE;
    } else {
      return "";
    }
  }

  private UnifiedAsset createSellerContact(
      CreateSellerContactRequest request,
      String sellerContactKey,
      String productCategory,
      UnifiedAssetDto componentAssetDto) {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            COMPONENT_SELLER_CONTACT.getKind(), sellerContactKey, SELLER_CONTACT_PREFIX);
    unifiedAsset.getMetadata().setDescription(SELLER_CONTACT_DESC);
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(COMPONENT_KEY, componentAssetDto.getMetadata().getKey());
    unifiedAsset.getMetadata().getLabels().put(productCategory, String.valueOf(Boolean.TRUE));

    SellerContactFacets facets = new SellerContactFacets();
    SellerContactFacets.SellerInfo sellerInfo = new SellerContactFacets.SellerInfo();
    sellerInfo.setRole(whichRole(componentAssetDto));
    sellerInfo.setName(request.getContactName());
    sellerInfo.setNumber(request.getContactPhone());
    sellerInfo.setEmailAddress(request.getContactEmail());
    facets.setSellerInfo(sellerInfo);
    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    return unifiedAsset;
  }

  public IngestionDataResult updateOne(
      String productId,
      String componentId,
      String id,
      UpdateSellerContactRequest request,
      String updatedBy) {
    UnifiedAssetDto unifiedAsset = unifiedAssetService.findOne(id);

    SellerContactFacets facets =
        UnifiedAsset.getFacets(unifiedAsset, new TypeReference<SellerContactFacets>() {});
    SellerContactFacets.SellerInfo sellerInfo =
        (null == facets.getSellerInfo()
            ? new SellerContactFacets.SellerInfo()
            : facets.getSellerInfo());
    sellerInfo.setName(request.getContactName());
    sellerInfo.setNumber(request.getContactPhone());
    sellerInfo.setEmailAddress(request.getContactEmail());
    facets.setSellerInfo(sellerInfo);
    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), updatedBy);
    IngestionDataResult syncResult =
        unifiedAssetService.syncAsset(productId, unifiedAsset, syncMetadata, true);
    if (syncResult.getCode() != HttpStatus.OK.value()) {
      throw new KrakenException(syncResult.getCode(), syncResult.getMessage());
    }
    log.info(
        "Seller contact asset:{} has been updated by:{}, componentId:{}, productId:{}",
        request,
        updatedBy,
        componentId,
        productId);

    return syncResult;
  }
}
