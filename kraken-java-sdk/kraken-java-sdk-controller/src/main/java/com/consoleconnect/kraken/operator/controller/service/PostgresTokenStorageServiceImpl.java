package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.entity.TokenStorageEntity;
import com.consoleconnect.kraken.operator.controller.repo.TokenStorageRepository;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class PostgresTokenStorageServiceImpl implements TokenStorageService {
  private final TokenStorageRepository tokenStorageRepository;

  @Override
  public void writeSecret(BuyerAssetDto value, String createdBy) {
    BuyerAssetDto.BuyerToken buyerToken = value.getBuyerToken();
    TokenStorageEntity entity = tokenStorageRepository.findFirstByAssetId(value.getId());
    if (entity == null) {
      TokenStorageEntity token = new TokenStorageEntity();
      token.setToken(buyerToken.getAccessToken());
      token.setExpiredAt(buyerToken.getExpiredAt());
      token.setAssetId(value.getId());
      token.setUpdatedBy(createdBy);
      tokenStorageRepository.save(token);
    } else {
      entity.setToken(buyerToken.getAccessToken());
      entity.setExpiredAt(buyerToken.getExpiredAt());
      entity.setCreatedBy(createdBy);
      tokenStorageRepository.save(entity);
    }
  }

  @Override
  public BuyerAssetDto.BuyerToken readSecret(String id) {
    log.info("postgres storage");
    TokenStorageEntity entity = tokenStorageRepository.findFirstByAssetId(id);
    if (entity == null) {
      throw KrakenException.notFound("token not found");
    }
    BuyerAssetDto.BuyerToken token = new BuyerAssetDto.BuyerToken();
    token.setAccessToken(entity.getToken());
    token.setExpiredAt(entity.getExpiredAt());
    return token;
  }
}
