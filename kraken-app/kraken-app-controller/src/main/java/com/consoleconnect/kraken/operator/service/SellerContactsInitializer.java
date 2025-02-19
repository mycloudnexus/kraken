package com.consoleconnect.kraken.operator.service;


import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.service.SellerContactService;
import com.consoleconnect.kraken.operator.core.service.AssetKeyGenerator;
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
public class SellerContactsInitializer implements AssetKeyGenerator {
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
    if (Objects.isNull(sellerContact)) {
      return;
    }
    String finalProductId =
        unifiedAssetService.existed(sellerContact.getKey()) ? sellerContact.getKey() : null;
    sellerContact
        .getSellerContactDetails()
        .forEach(detail -> processSellerContactDetail(finalProductId, detail));
  }

  private void processSellerContactDetail(
      String finalProductId, CreateSellerContactRequest detail) {
    String sellerContactKey =
        generateSellerContactKey(detail.getComponentKey(), detail.getParentProductType());
    boolean exist = unifiedAssetService.existed(sellerContactKey);
    if (exist) {
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
}
