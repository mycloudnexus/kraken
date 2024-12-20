package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.SellerContactChecker;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Getter
@AllArgsConstructor
@Service
@Slf4j
public class SellerContactService implements SellerContactChecker {
  private final UnifiedAssetService unifiedAssetService;

  public IngestionDataResult createSellerContact(
      String productId, String componentId, CreateSellerContactRequest request, String createdBy) {
    check(productId, componentId, request);
    UnifiedAssetDto unifiedAssetEntity = unifiedAssetService.findOne(componentId);

    return null;
  }

  public Boolean deleteSellerContact(String componentId, String id, String deletedBy) {
    return true;
  }
}
