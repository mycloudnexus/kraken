package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserTokenRequest;
import com.consoleconnect.kraken.operator.auth.entity.UserTokenEntity;
import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import com.consoleconnect.kraken.operator.auth.jwt.JwtEncoderToolkit;
import com.consoleconnect.kraken.operator.auth.mapper.UserTokenMapper;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.UserToken;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserTokenService;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.mapper.APITokenMapper;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.repo.APITokenRepository;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@AllArgsConstructor
public class APITokenService {
  private final APITokenRepository apiTokenRepository;
  private final AuthDataProperty.AuthServer authServer;
  private final EnvironmentRepository environmentRepository;
  private final Map<String, String> envCache = new ConcurrentHashMap<>();

  private final UserTokenService userTokenService;

  public static final String PRODUCT_ID = "productId";
  public static final String ENV_ID = "envId";

  @Transactional
  public APIToken createToken(String productId, CreateAPITokenRequest request, String createdBy) {
    if (!authServer.isEnabled()) {
      log.warn("auth server is disabled, enable it to support api token");
      throw KrakenException.badRequest(
          "Auth server is disabled, enable auth server to support api token");
    }

    if (request.getTokenExpiresInSeconds() <= 0) {
      request.setTokenExpiresInSeconds(5 * 365 * 86400L);
    }

    if (createdBy == null) {
      createdBy = UserContext.ANONYMOUS;
    }

    if (request.getUserId() == null) {
      request.setUserId(createdBy);
    }
    if (request.getRole() == null) {
      request.setRole(UserRoleEnum.API_CLIENT.name());
    }
    JwtEncoderToolkit jwtEncoderToolkit = JwtEncoderToolkit.get(authServer.getJwt());
    Map<String, Object> claims = new HashMap<>();
    claims.put(PRODUCT_ID, productId);
    claims.put(ENV_ID, request.getEnvId());
    claims.put(UserContext.TOKEN_CLAIM_ROLES, List.of(request.getRole()));
    String token =
        jwtEncoderToolkit.generateToken(createdBy, claims, request.getTokenExpiresInSeconds());

    CreateUserTokenRequest createUserTokenRequest =
        getCreateUserTokenRequest(request, token, claims);
    UserToken userToken = userTokenService.createToken(createUserTokenRequest, createdBy);
    APIToken apiToken = toAPIToken(userToken);
    apiToken.setToken(token);
    return apiToken;
  }

  private static @NotNull CreateUserTokenRequest getCreateUserTokenRequest(
      CreateAPITokenRequest request, String token, Map<String, Object> claims) {
    CreateUserTokenRequest createUserTokenRequest = new CreateUserTokenRequest();
    createUserTokenRequest.setToken(token);
    createUserTokenRequest.setTokenType(UserTokenTypeEnum.API_TOKEN);
    createUserTokenRequest.setUserId(request.getUserId());
    createUserTokenRequest.setName(request.getName());
    createUserTokenRequest.setMaxLifeEndInSeconds(request.getTokenExpiresInSeconds());
    createUserTokenRequest.setExpiresInSeconds(request.getTokenExpiresInSeconds());
    createUserTokenRequest.setClaims(claims);
    return createUserTokenRequest;
  }

  @Transactional(readOnly = true)
  public Paging<APIToken> search(
      String productId, String envId, boolean revoked, boolean expiredIncluded, Pageable pageable) {
    long expiresAtAfter = expiredIncluded ? 0 : System.currentTimeMillis();
    log.info(
        "Searching API tokens, revoked:{},expiresAtAfter:{}, envId:{}, productId:{}",
        revoked,
        expiresAtAfter,
        envId,
        productId);
    Page<UserTokenEntity> data =
        apiTokenRepository.search(
            UserTokenTypeEnum.API_TOKEN.toString().toUpperCase(),
            revoked,
            expiresAtAfter,
            envId,
            productId,
            pageable);
    return PagingHelper.toPaging(data, x -> this.toAPIToken(UserTokenMapper.INSTANCE.toToken(x)));
  }

  @Transactional(readOnly = true)
  public APIToken findOne(String id) {
    UserToken userToken = userTokenService.findOne(id);
    return toAPIToken(userToken);
  }

  @Transactional
  public APIToken revokeToken(String id, String requestedBy) {
    UserToken userToken = userTokenService.revokeToken(id, requestedBy);
    return toAPIToken(userToken);
  }

  private APIToken toAPIToken(UserToken userToken) {
    APIToken apiToken = APITokenMapper.INSTANCE.toAPIToken(userToken);
    apiToken.setEnvId((String) userToken.getClaims().get(ENV_ID));
    apiToken.setProductId((String) userToken.getClaims().get(PRODUCT_ID));
    return apiToken;
  }

  public Optional<APIToken> findOneByAuth(String token) {
    return userTokenService.findOneByToken(token).map(this::toAPIToken);
  }

  public String findEnvId(String authorization, String envIdOrKey) {
    if (authorization == null || !StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
      String envId =
          envIdOrKey == null ? null : envCache.computeIfAbsent(envIdOrKey, this::findEnvId);
      if (envId == null) {
        throw KrakenException.badRequest("Invalid envId");
      }
      return envId;
    }
    String bearerToken = authorization.substring(7).trim();
    return findOneByAuth(bearerToken)
        .map(APIToken::getEnvId)
        .orElseThrow(
            () -> {
              log.warn("No envId found based on the token");
              return KrakenException.unauthorized("Invalid Token");
            });
  }

  private String findEnvId(String envNameOrId) {
    return environmentRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE)).stream()
        .filter(
            env ->
                env.getId().toString().equalsIgnoreCase(envNameOrId)
                    || env.getName().equalsIgnoreCase(envNameOrId))
        .map(EnvironmentEntity::getId)
        .findFirst()
        .map(UUID::toString)
        .orElseThrow(() -> KrakenException.notFound("Environment not found"));
  }
}
