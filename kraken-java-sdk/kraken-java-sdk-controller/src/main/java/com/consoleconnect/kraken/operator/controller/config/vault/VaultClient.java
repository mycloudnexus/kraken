package com.consoleconnect.kraken.operator.controller.config.vault;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Service
public class VaultClient {

  protected final VaultTemplate vaultTemplate;
  protected final VaultProperty vaultProperty;

  public VaultClient(
      @Autowired(required = false) VaultTemplate vaultTemplate,
      @Autowired VaultProperty vaultProperty) {
    this.vaultTemplate = vaultTemplate;
    this.vaultProperty = vaultProperty;
  }

  protected String generatePath(String endpoint) {
    return String.format("%s%s", vaultProperty.getNamespace(), endpoint);
  }

  public <T> HttpResponse<T> write(String endpoint, Object payload, TypeReference<T> valueTypeRef) {
    if (this.vaultTemplate == null) {
      return HttpResponse.of(
          HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), null);
    }
    try {
      VaultData<Object> data = new VaultData<>();
      data.setData(payload);
      VaultResponse vaultRes = this.vaultTemplate.write(this.generatePath(endpoint), data);
      if (vaultRes == null) {
        return HttpResponse.of(
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null);
      }
      return handleResponse(valueTypeRef, vaultRes);
    } catch (VaultException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  public <T> HttpResponse<T> read(String endpoint, TypeReference<T> valueTypeRef) {
    if (this.vaultTemplate == null) {
      return HttpResponse.of(
          HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), null);
    }

    try {
      VaultResponse vaultRes = this.vaultTemplate.read(this.generatePath(endpoint));
      if (vaultRes == null) {
        return HttpResponse.of(
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null);
      }
      return handleResponse(valueTypeRef, vaultRes);
    } catch (VaultException ex) {
      throw KrakenException.internalError(ex.getMessage());
    }
  }

  private static <T> HttpResponse<T> handleResponse(
      TypeReference<T> valueTypeRef, VaultResponse vaultRes) {
    Map<String, Object> vaultPayload = vaultRes.getData();
    if (vaultPayload == null) {
      return HttpResponse.of(
          HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null);
    }
    Object vaultData = vaultPayload.get("data");
    if (vaultData == null) {
      return HttpResponse.of(
          HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null);
    }
    T data = JsonToolkit.fromJson(JsonToolkit.toJson(vaultData), valueTypeRef);
    return HttpResponse.ok(data);
  }
}
