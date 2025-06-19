package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.SecurityChecker;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

@Component(value = "securityBuyerChecker")
@AllArgsConstructor
public class BuyerCheckerService implements SecurityChecker {

  public static final int INTERVAL = 60;
  private static final String INVALID_ENV_TOKEN =
      "The provided token belongs to the %s cannot be used in the %s environment. Please use a valid token.";
  private UnifiedAssetService unifiedAssetService;
  private AuthDataProperty.ResourceServer resourceServer;

  @Override
  public Mono<Object> internalRun(ServerWebExchange exchange) {
    return ReactiveSecurityContextHolder.getContext()
        .handle(getSecurityContextSynchronousSinkBiConsumer(exchange));
  }

  public BiConsumer<SecurityContext, SynchronousSink<Object>>
      getSecurityContextSynchronousSinkBiConsumer(ServerWebExchange exchange) {
    return (securityContext, sink) -> {
      Authentication authentication = securityContext.getAuthentication();
      List<UnifiedAssetDto> list =
          unifiedAssetService
              .findBySpecification(
                  Tuple2.ofList(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_BUYER.getKind()),
                  Tuple2.ofList(LabelConstants.LABEL_BUYER_ID, authentication.getName()),
                  null,
                  PageRequest.ofSize(1),
                  null)
              .getData();
      if (CollectionUtils.isEmpty(list)) {
        sink.error(KrakenException.badRequest("buyer not found"));
        return;
      }
      UnifiedAssetDto unifiedAssetDto = list.get(0);
      if (AssetStatusEnum.DEACTIVATED
          .getKind()
          .equalsIgnoreCase(unifiedAssetDto.getMetadata().getStatus())) {
        sink.error(KrakenException.badRequest("buyer deactivated"));
        return;
      }
      Map<String, String> labels = unifiedAssetDto.getMetadata().getLabels();
      Instant dbGeneratedAt =
          Optional.ofNullable(labels.get(LabelConstants.LABEL_ISSUE_AT))
              .map(DateTimeFormatter.ISO_INSTANT::parse)
              .map(Instant::from)
              .orElse(Instant.MIN);
      Jwt principal = (Jwt) authentication.getPrincipal();
      String signEnv =
          Optional.ofNullable(principal.getClaims())
              .map(map -> map.get(Constants.ENV))
              .map(Object::toString)
              .orElse("");
      // To compatible with the old token which has no environment salt.
      if (StringUtils.isNotBlank(signEnv)
          && !StringUtils.equalsIgnoreCase(resourceServer.getVerifier().getEnv(), signEnv)) {
        String error =
            String.format(INVALID_ENV_TOKEN, signEnv, resourceServer.getVerifier().getEnv());
        sink.error(KrakenException.unauthorizedInvalidCredentials(error));
        return;
      }
      Instant issuedAt = principal.getIssuedAt();
      if (Objects.requireNonNull(issuedAt).isBefore(dbGeneratedAt.minusSeconds(INTERVAL))) {
        sink.error(KrakenException.badRequest("Token expired "));
        return;
      }
      exchange
          .getAttributes()
          .put(KrakenFilterConstants.X_KRAKEN_CURRENT_ENV, resourceServer.getVerifier().getEnv());
      sink.next(new Object());
    };
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
