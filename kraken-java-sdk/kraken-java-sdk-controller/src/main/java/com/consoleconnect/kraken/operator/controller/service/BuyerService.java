package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.auth.jwt.JwtEncoderToolkit;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateBuyerRequest;
import com.consoleconnect.kraken.operator.controller.mapper.BuyerAssetDtoMapper;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class BuyerService extends AssetStatusManager {
  private static final String BUYER_KEY_PREFIX = "mef.sonata.buyer";
  private static final String BUYER_NAME = "Buyer";
  private static final String BUYER_DESC = "Onboard buyer information";

  @Getter private final UnifiedAssetService unifiedAssetService;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final AuthDataProperty.AuthServer authServer;
  private final MgmtProperty appProperty;

  @Transactional
  public BuyerAssetDto create(String productId, CreateBuyerRequest buyerOnboard, String createdBy) {
    if (StringUtils.isBlank(buyerOnboard.getBuyerId())) {
      throw KrakenException.badRequest("buyerId is mandatory");
    }
    if (StringUtils.isBlank(buyerOnboard.getEnvId())) {
      throw KrakenException.badRequest("envId is mandatory");
    }

    Page<UnifiedAssetEntity> exist =
        unifiedAssetRepository.findBuyers(
            null,
            PRODUCT_BUYER.getKind(),
            buyerOnboard.getEnvId(),
            buyerOnboard.getBuyerId(),
            AssetStatusEnum.ACTIVATED.getKind(),
            null,
            PageRequest.of(0, 1));
    if (CollectionUtils.isNotEmpty(exist.getContent())) {
      throw KrakenException.badRequest(
          "The buyer has existed in the current environment, buyerId:"
              + buyerOnboard.getBuyerId()
              + ", envId:"
              + buyerOnboard.getEnvId());
    }

    UnifiedAsset buyer =
        createBuyer(
            buyerOnboard.getBuyerId(), buyerOnboard.getEnvId(), buyerOnboard.getCompanyName());
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    IngestionDataResult syncResult =
        unifiedAssetService.syncAsset(productId, buyer, syncMetadata, true);
    if (syncResult.getCode() != HttpStatus.OK.value()) {
      throw new KrakenException(syncResult.getCode(), syncResult.getMessage());
    }

    UnifiedAssetDto buyerCreated =
        unifiedAssetService.findOne(syncResult.getData().getId().toString());
    return generateBuyer(
        buyerCreated, buyerOnboard.getBuyerId(), buyerOnboard.getTokenExpiredInSeconds());
  }

  @Transactional(readOnly = true)
  public Paging<UnifiedAssetDto> search(
      String parentId,
      String envId,
      String buyerId,
      String status,
      ZonedDateTime lastUpdateTime,
      PageRequest pageRequest) {
    if (parentId != null) {
      parentId = unifiedAssetService.findOneByIdOrKey(parentId).getId().toString();
    }
    Page<UnifiedAssetEntity> data =
        unifiedAssetRepository.findBuyers(
            parentId, PRODUCT_BUYER.getKind(), envId, buyerId, status, lastUpdateTime, pageRequest);
    return PagingHelper.toPaging(data, entity -> UnifiedAssetService.toAsset(entity, true));
  }

  @Transactional
  public BuyerAssetDto regenerate(
      String productId, String id, Long tokenExpiredInSeconds, String createdBy) {
    log.info(
        "regenerate buyer token, productId:{}, id:{}, tokenExpiredInSeconds:{}",
        productId,
        id,
        tokenExpiredInSeconds);
    UnifiedAssetDto buyer = unifiedAssetService.findOne(id);
    BuyerOnboardFacets buyerOnboardFacets =
        UnifiedAsset.getFacets(buyer, new TypeReference<BuyerOnboardFacets>() {});
    BuyerOnboardFacets.BuyerInfo buyerInfo = buyerOnboardFacets.getBuyerInfo();
    if (Objects.isNull(buyerInfo)) {
      throw KrakenException.notFound("The buyer information is not existed.");
    }
    if (!AssetStatusEnum.ACTIVATED.getKind().equals(buyer.getMetadata().getStatus())) {
      throw KrakenException.badRequest("The buyer is not activated.");
    }
    BuyerAssetDto buyerAssetDto =
        generateBuyer(buyer, buyerInfo.getBuyerId(), tokenExpiredInSeconds);
    buyer
        .getMetadata()
        .getLabels()
        .put(LABEL_ISSUE_AT, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    unifiedAssetService.syncAsset(productId, buyer, syncMetadata, true);
    return buyerAssetDto;
  }

  private BuyerAssetDto generateBuyer(
      UnifiedAssetDto unifiedAssetDto, String buyerId, Long tokenExpiredInSeconds) {
    BuyerAssetDto buyerAssetDto = BuyerAssetDtoMapper.INSTANCE.toBuyerAssetDto(unifiedAssetDto);
    BuyerAssetDto.BuyerToken buyerToken = generateBuyerToken(buyerId, tokenExpiredInSeconds);
    buyerAssetDto.setBuyerToken(buyerToken);
    return buyerAssetDto;
  }

  private BuyerAssetDto.BuyerToken generateBuyerToken(String buyerId, Long tokenExpiredInSeconds) {
    if (null == tokenExpiredInSeconds || tokenExpiredInSeconds <= 0) {
      tokenExpiredInSeconds =
          appProperty.getBuyerTokenExpiredSeconds() == null
              ? Long.valueOf(MgmtProperty.DEFAULT_TOKEN_EXPIRED_SECONDS)
              : Long.valueOf(appProperty.getBuyerTokenExpiredSeconds());
    }
    BuyerAssetDto.BuyerToken buyerToken = new BuyerAssetDto.BuyerToken();
    if (authServer.isEnabled()) {
      String token =
          JwtEncoderToolkit.get(authServer.getJwt())
              .generateToken(buyerId, null, tokenExpiredInSeconds);
      buyerToken.setExpiredAt(
          Date.from(ZonedDateTime.now().plusSeconds(tokenExpiredInSeconds).toInstant()));
      buyerToken.setAccessToken(token);
    } else {
      log.warn("The authentication server is disabled, skip to generate a token");
    }
    return buyerToken;
  }

  private UnifiedAsset createBuyer(String buyerId, String envId, String companyName) {
    String key = BUYER_KEY_PREFIX + System.currentTimeMillis();
    UnifiedAsset unifiedAsset = UnifiedAsset.of(PRODUCT_BUYER.getKind(), key, BUYER_NAME);
    unifiedAsset.getMetadata().setDescription(BUYER_DESC);
    unifiedAsset.getMetadata().getLabels().put(LABEL_ENV_ID, envId);
    unifiedAsset.getMetadata().getLabels().put(LABEL_BUYER_ID, buyerId);
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(LABEL_ISSUE_AT, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    BuyerOnboardFacets facets = new BuyerOnboardFacets();
    BuyerOnboardFacets.BuyerInfo buyerInfo = new BuyerOnboardFacets.BuyerInfo();
    buyerInfo.setBuyerId(buyerId);
    buyerInfo.setEnvId(envId);
    buyerInfo.setCompanyName(companyName);
    facets.setBuyerInfo(buyerInfo);

    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    return unifiedAsset;
  }

  @Transactional
  public BuyerAssetDto activate(String productId, String id, String createdBy) {
    UnifiedAssetDto assetDto = activateAsset(id, PRODUCT_BUYER, "buyer not found");

    BuyerOnboardFacets facets = UnifiedAsset.getFacets(assetDto, BuyerOnboardFacets.class);
    String buyerId = facets.getBuyerInfo().getBuyerId();
    BuyerAssetDto buyerAssetDto = generateBuyer(assetDto, buyerId, null);

    // add labels
    Map<String, String> labels = assetDto.getMetadata().getLabels();
    labels.put(LABEL_ISSUE_AT, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    labels.put(
        LABEL_EXPIRED_AT,
        DateTimeFormatter.ISO_INSTANT.format(
            buyerAssetDto.getBuyerToken().getExpiredAt().toInstant()));

    afterCompletion(assetDto, createdBy);
    return buyerAssetDto;
  }

  @Transactional
  public Boolean deactivate(String productId, String id, String createdBy) {
    UnifiedAssetDto assetDto = deactivateAsset(id, PRODUCT_BUYER, "buyer not found");

    // add labels
    assetDto
        .getMetadata()
        .getLabels()
        .put(LABEL_EXPIRED_AT, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

    afterCompletion(assetDto, createdBy);
    return true;
  }
}
