package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.AppMgmtProperty;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.service.SellerContactService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.service.AssetKeyGenerator;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class SellerContactsInitializer implements AssetKeyGenerator {
  private final AppMgmtProperty mgmtProperty;
  private final SellerContactService sellerContactService;
  private final UnifiedAssetService unifiedAssetService;

  @EventListener(PlatformSettingCompletedEvent.class)
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
    UnifiedAssetDto current = unifiedAssetService.findOneIfExist(sellerContactKey);
    if (Objects.nonNull(current)) {
      log.info("seller contact key has exist:{}, no need to create", sellerContactKey);
      List<UnifiedAssetDto> list = unifiedAssetService.findByKind(AssetKindEnum.PRODUCT.getKind());
      String productKey =
          Optional.ofNullable(list.get(0))
              .map(UnifiedAssetDto::getMetadata)
              .map(Metadata::getKey)
              .orElse(null);
      // update parent id here
      current.setParentId(productKey);
      SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), "system");
      unifiedAssetService.syncAsset(productKey, current, syncMetadata, true);
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
