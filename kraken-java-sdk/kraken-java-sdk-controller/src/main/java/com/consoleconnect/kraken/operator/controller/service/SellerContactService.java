package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_SELLER_CONTACT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.*;

import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.dto.UpdateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
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
import org.apache.commons.lang3.StringUtils;
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
  public IngestionDataResult createSellerContact(
      String productId, String componentId, CreateSellerContactRequest request, String createdBy) {
    UnifiedAssetDto componentAssetDto = unifiedAssetService.findOne(componentId);
    return createOneSellerContact(
        productId, componentAssetDto.getMetadata().getKey(), request, createdBy);
  }

  public IngestionDataResult createOneSellerContact(
      String productId, String componentKey, CreateSellerContactRequest request, String createdBy) {
    String sellerContactKey = generateKey(componentKey, request.getParentProductType());
    UnifiedAsset sellerAsset =
        createSellerContact(
            request, sellerContactKey, request.getParentProductType(), componentKey);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    return unifiedAssetService.syncAsset(productId, sellerAsset, syncMetadata, true);
  }

  private String generateKey(String componentKey, String parentProductType) {
    if (StringUtils.isBlank(parentProductType)) {
      return componentKey
          + DOT
          + ParentProductTypeEnum.ACCESS_ELINE.getKind()
          + DOT
          + SELLER_CONTACT_SUFFIX;
    }
    return componentKey + DOT + parentProductType + DOT + SELLER_CONTACT_SUFFIX;
  }

  private String whichRole(String componentKey) {
    if (componentKey.contains(QUOTE_KEY_WORD)) {
      return QUOTE_ROLE;
    } else if (componentKey.contains(ORDER_KEY_WORD)) {
      return ORDER_ROLE;
    } else {
      return "";
    }
  }

  private UnifiedAsset createSellerContact(
      CreateSellerContactRequest request,
      String sellerContactKey,
      String parentProductType,
      String componentKey) {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            COMPONENT_SELLER_CONTACT.getKind(), sellerContactKey, SELLER_CONTACT_PREFIX);
    unifiedAsset.getMetadata().setDescription(SELLER_CONTACT_DESC);
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    unifiedAsset.getMetadata().getLabels().put(COMPONENT_KEY, componentKey);
    unifiedAsset.getMetadata().getLabels().put(parentProductType, String.valueOf(Boolean.TRUE));

    SellerContactFacets facets = new SellerContactFacets();
    SellerContactFacets.SellerInfo sellerInfo = new SellerContactFacets.SellerInfo();
    sellerInfo.setRole(whichRole(componentKey));
    sellerInfo.setName(request.getName());
    sellerInfo.setNumber(request.getNumber());
    sellerInfo.setEmailAddress(request.getEmailAddress());
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
    sellerInfo.setName(request.getName());
    sellerInfo.setNumber(request.getNumber());
    sellerInfo.setEmailAddress(request.getEmailAddress());
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
