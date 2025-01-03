package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ProductCategoryEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public interface SellerContactChecker {

  String COMPONENT_KEY = "componentKey";

  UnifiedAssetRepository getUnifiedAssetRepository();

  default void checkSellerContacts(
      String productId, String componentId, CreateSellerContactRequest request) {
    if (StringUtils.isBlank(request.getContactName())) {
      throw KrakenException.badRequest("The contactName is mandatory");
    }
    if (StringUtils.isBlank(request.getContactEmail())) {
      throw KrakenException.badRequest("The contactEmail is mandatory");
    }
    if (StringUtils.isBlank(request.getContactPhone())) {
      throw KrakenException.badRequest("The contactPhone is mandatory");
    }
    if (CollectionUtils.isEmpty(request.getProductCategories())) {
      throw KrakenException.badRequest("The productTypes are mandatory");
    }
    request
        .getProductCategories()
        .forEach(
            item -> {
              if (Arrays.stream(ProductCategoryEnum.values())
                  .noneMatch(category -> category.getKind().equals(item))) {
                throw KrakenException.badRequest(
                    "productTypes should be in: ["
                        + Stream.of(ProductCategoryEnum.values())
                            .map(ProductCategoryEnum::getKind)
                            .toList()
                        + "]");
              }
            });
  }

  default void checkExisted(List<String> keyList) {
    List<UnifiedAssetEntity> existed = getUnifiedAssetRepository().findAllByKeyIn(keyList);
    if (CollectionUtils.isNotEmpty(existed)) {
      throw KrakenException.badRequest(
          "The seller contact with same componentKey and productTypes has existed.");
    }
  }
}
