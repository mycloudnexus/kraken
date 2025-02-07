package com.consoleconnect.kraken.operator.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.DOT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.SELLER_CONTACT_SUFFIX;

import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.service.SellerContactService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import jakarta.annotation.PostConstruct;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class SellerContactsInitializer {
  private final AppMgmtProperty mgmtProperty;
  private final SellerContactService sellerContactService;
  private final UnifiedAssetService unifiedAssetService;

  @PostConstruct
  public void initialize() {
    log.info("Initializing seller contacts: {}", mgmtProperty.getSellerContacts());
    mgmtProperty.getSellerContacts().forEach(this::processSellerContact);
    log.info("initialize seller contacts done");
  }

  private void processSellerContact(AppMgmtProperty.SellerContact sellerContact) {
    String finalProductId = fetchProduct(sellerContact.getKey());
    sellerContact
        .getSellerContactDetails()
        .forEach(detail -> processSellerContactDetail(finalProductId, detail));
  }

  private void processSellerContactDetail(
      String finalProductId, CreateSellerContactRequest detail) {
    UnifiedAssetDto contactAssetDto = null;
    String sellerContactKey = generateSellerContactKey(detail);
    try {
      contactAssetDto = unifiedAssetService.findOne(sellerContactKey);
    } catch (KrakenException e) {
      log.error("Not found seller contact", e);
    }
    if (Objects.nonNull(contactAssetDto)) {
      log.info("seller contact key has exist:{}, no need to create", sellerContactKey);
      return;
    }
    createSellerContact(finalProductId, detail);
  }

  private void createSellerContact(String finalProductId, CreateSellerContactRequest detail) {
    CreateSellerContactRequest request = new CreateSellerContactRequest();
    request.setComponentKey(detail.getComponentKey());
    request.setParentProductType(detail.getParentProductType());
    request.setNumber(StringUtils.isBlank(detail.getNumber()) ? "" : detail.getNumber());
    request.setName(StringUtils.isBlank(detail.getName()) ? "" : detail.getName());
    request.setEmailAddress(
        StringUtils.isBlank(detail.getEmailAddress()) ? "" : detail.getEmailAddress());
    sellerContactService.createOneSellerContact(
        finalProductId, detail.getComponentKey(), request, "system");
  }

  private String generateSellerContactKey(CreateSellerContactRequest detail) {
    return detail.getComponentKey()
        + DOT
        + detail.getParentProductType()
        + DOT
        + SELLER_CONTACT_SUFFIX;
  }

  private String fetchProduct(String productId) {
    try {
      unifiedAssetService.findOne(productId);
      return productId;
    } catch (KrakenException e) {
      String error = String.format("Cannot find productId: %s", productId);
      log.error(error, e);
      return null;
    }
  }
}
