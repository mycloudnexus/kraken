package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;

public interface TokenStorageService {
  void writeSecret(BuyerAssetDto value, String createdBy);

  BuyerAssetDto.BuyerToken readSecret(String buyerId);
}
