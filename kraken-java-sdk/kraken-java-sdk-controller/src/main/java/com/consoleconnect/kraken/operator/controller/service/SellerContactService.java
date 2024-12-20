package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Getter
@AllArgsConstructor
@Service
@Slf4j
public class SellerContactService {

  public UnifiedAssetDto createSellerContact(
      String productId, String componentId, CreateSellerContactRequest request, String createdBy) {
    return null;
  }

  public Boolean deleteSellerContact(String componentId, String id, String deletedBy) {
    return true;
  }
}
