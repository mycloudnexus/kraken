package com.consoleconnect.kraken.operator.controller.config.vault;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.vault.core.VaultTemplate;

@Slf4j
class VaultClientTest {

  public static final String PATH = "path";

  @Test
  void givenEmptyTemplate_whenWrite_thenError() {
    VaultClient vaultClient = new VaultClient(null, new VaultProperty());
    Assertions.assertEquals(
        501, vaultClient.write(PATH, new Object(), new TypeReference<Object>() {}).getCode());
    Assertions.assertEquals(501, vaultClient.read(PATH, new TypeReference<Object>() {}).getCode());
  }

  @Test
  void givenVaultTemplate_whenWriteOrRead_thenThrowError() {
    VaultTemplate vaultTemplate = Mockito.mock(VaultTemplate.class);
    VaultClient vaultClient = new VaultClient(vaultTemplate, new VaultProperty());
    Mockito.when(vaultTemplate.write(anyString(), any())).thenThrow(KrakenException.class);
    Mockito.when(vaultTemplate.read(anyString())).thenThrow(KrakenException.class);
    Object o = new Object();
    TypeReference<Object> objectTypeReference = new TypeReference<>() {};
    Assertions.assertThrows(
        KrakenException.class, () -> vaultClient.write(PATH, o, objectTypeReference));
    Assertions.assertThrows(
        KrakenException.class, () -> vaultClient.read(PATH, objectTypeReference));
  }

  @Test
  void givenVaultTemplate_whenWriteOrRead_thenReturnNull() {
    VaultTemplate vaultTemplate = Mockito.mock(VaultTemplate.class);
    VaultClient vaultClient = new VaultClient(vaultTemplate, new VaultProperty());
    Mockito.when(vaultTemplate.write(anyString(), any())).thenReturn(null);
    Mockito.when(vaultTemplate.read(anyString())).thenReturn(null);
    Assertions.assertEquals(
        HttpStatus.NOT_FOUND.value(),
        vaultClient.write(PATH, new Object(), new TypeReference<Object>() {}).getCode());
    Assertions.assertEquals(
        HttpStatus.NOT_FOUND.value(),
        vaultClient.read(PATH, new TypeReference<Object>() {}).getCode());
  }
}
