package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.config.vault.VaultClient;
import com.consoleconnect.kraken.operator.controller.config.vault.VaultProperty;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

@Slf4j
@AllArgsConstructor
public class VaultTokenStorageServiceImpl implements TokenStorageService {
  private final VaultClient client;
  private final VaultProperty vaultProperty;

  @Override
  public void writeSecret(BuyerAssetDto value, String createdBy) {
    String endpoint =
        String.format(vaultProperty.getSecretMaterials().getBuyerTokenPath(), value.getId());
    client.write(endpoint, value.getBuyerToken(), new TypeReference<BuyerAssetDto.BuyerToken>() {});
  }

  @Override
  public BuyerAssetDto.BuyerToken readSecret(String id) {
    log.info("vault storage id: {}", id);
    String endpoint = String.format(vaultProperty.getSecretMaterials().getBuyerTokenPath(), id);
    HttpResponse<BuyerAssetDto.BuyerToken> response =
        client.read(endpoint, new TypeReference<BuyerAssetDto.BuyerToken>() {});
    if (response.getCode() == HttpStatus.OK.value()
        && response.getData() != null
        && StringUtils.isNotBlank(response.getData().getAccessToken())) {
      return response.getData();
    }
    throw KrakenException.notFound("Token not found in vault storage");
  }
}
