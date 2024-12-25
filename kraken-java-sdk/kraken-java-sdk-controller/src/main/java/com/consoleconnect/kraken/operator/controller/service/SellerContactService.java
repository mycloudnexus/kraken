package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_SELLER_CONTACT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.DOT;

import com.consoleconnect.kraken.operator.controller.SellerContactChecker;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Getter
@AllArgsConstructor
@Service
@Slf4j
public class SellerContactService implements SellerContactChecker {
  private static final String SELLER_CONTACT_PREFIX = "mef.sonata.seller.contact";
  private static final String SELLER_CONTACT_DESC = "seller contact information";
  private final UnifiedAssetService unifiedAssetService;
  @Getter private final UnifiedAssetRepository unifiedAssetRepository;

  @Transactional
  public IngestionDataResult createSellerContact(
      String productId, String componentId, CreateSellerContactRequest request, String createdBy) {
    checkSellerContacts(productId, componentId, request);

    UnifiedAssetDto componentAssetDto = unifiedAssetService.findOne(componentId);
    List<String> productTypes = request.getProductTypes();
    Collections.sort(productTypes);
    String key = componentAssetDto.getMetadata().getKey() + DOT + String.join(DOT, productTypes);

    List<String> keyList = new ArrayList<>();
    keyList.add(key);
    if (request.getProductTypes().size() == 1) {
      keyList.add(
          componentAssetDto.getMetadata().getKey()
              + DOT
              + String.join(
                  DOT,
                  Arrays.stream(ProductCategoryEnum.values())
                      .map(ProductCategoryEnum::getKind)
                      .toList()));
    }

    checkExisted(keyList);

    UnifiedAsset sellerAsset = createSellerContact(request, key, componentAssetDto);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    return unifiedAssetService.syncAsset(productId, sellerAsset, syncMetadata, true);
  }

  private UnifiedAsset createSellerContact(
      CreateSellerContactRequest request, String key, UnifiedAssetDto componentAssetEntity) {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(COMPONENT_SELLER_CONTACT.getKind(), key, SELLER_CONTACT_PREFIX);
    unifiedAsset.getMetadata().setDescription(SELLER_CONTACT_DESC);
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(COMPONENT_KEY, componentAssetEntity.getMetadata().getKey());
    request
        .getProductTypes()
        .forEach(
            item -> unifiedAsset.getMetadata().getLabels().put(item, String.valueOf(Boolean.TRUE)));

    SellerContactFacets facets = new SellerContactFacets();
    SellerContactFacets.SellerInfo sellerInfo = new SellerContactFacets.SellerInfo();
    sellerInfo.setContactName(request.getContactName());
    sellerInfo.setContactPhone(request.getContactPhone());
    sellerInfo.setContactEmail(request.getContactEmail());
    facets.setSellerInfo(sellerInfo);
    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    return unifiedAsset;
  }

  public Boolean deleteSellerContact(
      String productId, String componentId, String id, String deletedBy) {
    UnifiedAssetDto sellerContactAssetDto = unifiedAssetService.findOne(id);
    if (Objects.nonNull(sellerContactAssetDto)) {
      unifiedAssetService.deleteOne(sellerContactAssetDto.getMetadata().getKey());
      log.info(
          "Seller contact asset:{} is deleted by:{}, componentId:{}, productId:{}",
          id,
          deletedBy,
          componentId,
          productId);
    }
    return true;
  }
}
