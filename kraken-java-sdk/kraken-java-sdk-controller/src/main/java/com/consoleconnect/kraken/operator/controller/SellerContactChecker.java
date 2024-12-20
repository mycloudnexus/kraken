package com.consoleconnect.kraken.operator.controller;

import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public interface SellerContactChecker {

  default void check(String productId, String componentId, CreateSellerContactRequest request) {
    if (StringUtils.isBlank(productId) || StringUtils.isBlank(componentId)) {
      throw KrakenException.badRequest("productId and componentId are mandatory");
    }
    if (CollectionUtils.isEmpty(request.getProductTypes())) {
      throw KrakenException.badRequest("productTypes are mandatory");
    }
  }
}
