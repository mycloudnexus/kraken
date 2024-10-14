package com.consoleconnect.kraken.operator.auth.service;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserTokenRequest;
import com.consoleconnect.kraken.operator.auth.dto.UpdateUserTokenRequest;
import com.consoleconnect.kraken.operator.auth.entity.UserTokenEntity;
import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import com.consoleconnect.kraken.operator.auth.jwt.JwtEncoderToolkit;
import com.consoleconnect.kraken.operator.auth.mapper.UserTokenMapper;
import com.consoleconnect.kraken.operator.auth.model.UserToken;
import com.consoleconnect.kraken.operator.auth.repo.UserTokenRepository;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class UserTokenService {
  private final UserTokenRepository apiTokenRepository;

  @Transactional
  public UserToken createToken(CreateUserTokenRequest request, String createdBy) {
    UserTokenEntity apiTokenEntity = new UserTokenEntity();
    apiTokenEntity.setName(request.getName());
    apiTokenEntity.setUserId(request.getUserId());
    apiTokenEntity.setToken(JwtEncoderToolkit.hashToken(request.getToken()));
    apiTokenEntity.setTokenType(request.getTokenType());
    apiTokenEntity.setExpiresAt(System.currentTimeMillis() + request.getExpiresInSeconds() * 1000);
    apiTokenEntity.setMaxLifeEndAt(
        System.currentTimeMillis() + request.getMaxLifeEndInSeconds() * 1000);
    apiTokenEntity.setCreatedBy(createdBy);
    apiTokenEntity.setCreatedAt(DateTime.nowInUTC());
    apiTokenEntity.setClaims(request.getClaims() == null ? new HashMap<>() : request.getClaims());
    apiTokenEntity.setMetadata(
        request.getMetadata() == null ? new HashMap<>() : request.getMetadata());
    apiTokenEntity = apiTokenRepository.save(apiTokenEntity);
    return UserTokenMapper.INSTANCE.toToken(apiTokenEntity);
  }

  @Transactional(readOnly = true)
  public Paging<UserToken> search(
      String userId, boolean revoked, UserTokenTypeEnum tokenType, Pageable pageable) {
    long current = System.currentTimeMillis();
    Page<UserTokenEntity> data =
        apiTokenRepository
            .findAllByUserIdAndTokenTypeAndRevokedAndExpiresAtAfterAndMaxLifeEndAtAfter(
                userId, tokenType, revoked, current, current, pageable);
    return PagingHelper.toPaging(data, UserTokenMapper.INSTANCE::toToken);
  }

  @Transactional(readOnly = true)
  public UserToken findOne(String id) {
    return UserTokenMapper.INSTANCE.toToken(findOneEntity(id));
  }

  private final UserTokenEntity findOneEntity(String id) {
    return apiTokenRepository
        .findById(UUID.fromString(id))
        .orElseThrow(() -> KrakenException.notFound("Token not found"));
  }

  @Transactional
  public void revokeAllTokensByUserId(
      String userId, UserTokenTypeEnum tokenType, String requestBy) {
    log.info("Revoking all tokens for user {}, requestedBy:{}", userId, requestBy);
    long current = System.currentTimeMillis();
    apiTokenRepository
        .findAllByUserIdAndTokenTypeAndRevokedAndExpiresAtAfterAndMaxLifeEndAtAfter(
            userId, tokenType, false, current, current, PageRequest.of(0, Integer.MAX_VALUE))
        .stream()
        .forEach(
            apiTokenEntity -> {
              log.info("Revoking token {}", apiTokenEntity.getId());
              apiTokenEntity.setRevoked(true);
              apiTokenEntity.setRevokedAt(DateTime.nowInUTC());
              apiTokenEntity.setRevokedBy(requestBy);
              apiTokenEntity.setUpdatedBy(requestBy);
              apiTokenEntity.setUpdatedAt(DateTime.nowInUTC());
              apiTokenRepository.save(apiTokenEntity);
              log.info("Revoked token {}", apiTokenEntity.getId());
            });
    log.info("Revoked all tokens for user {}", userId);
  }

  @Transactional
  public UserToken revokeToken(String id, String requestedBy) {
    UserTokenEntity apiTokenEntity = findOneEntity(id);

    if (isTokenExpired(apiTokenEntity)) {
      throw KrakenException.badRequest("Token is expired");
    }

    if (requestedBy == null) {
      requestedBy = UserContext.ANONYMOUS;
    }
    apiTokenEntity.setRevoked(true);
    apiTokenEntity.setRevokedAt(DateTime.nowInUTC());
    apiTokenEntity.setRevokedBy(requestedBy);
    apiTokenEntity.setUpdatedBy(requestedBy);
    apiTokenEntity = apiTokenRepository.save(apiTokenEntity);
    return UserTokenMapper.INSTANCE.toToken(apiTokenEntity);
  }

  public Optional<UserToken> findOneByToken(String token) {
    return apiTokenRepository
        .findOneByToken(JwtEncoderToolkit.hashToken(token))
        .filter(entity -> !isTokenExpired(entity))
        .map(UserTokenMapper.INSTANCE::toToken);
  }

  private boolean isTokenExpired(UserTokenEntity apiTokenEntity) {
    return apiTokenEntity.isRevoked()
        || apiTokenEntity.getExpiresAt() < System.currentTimeMillis()
        || apiTokenEntity.getMaxLifeEndAt() < System.currentTimeMillis();
  }

  public UserToken updateToken(String id, UpdateUserTokenRequest request, String updatedBy) {
    UserTokenEntity apiTokenEntity = findOneEntity(id);

    if (isTokenExpired(apiTokenEntity)) {
      throw KrakenException.badRequest("Token is expired");
    }
    if (request.getToken() != null) {
      apiTokenEntity.setToken(JwtEncoderToolkit.hashToken(request.getToken()));
    }
    if (request.getExpiresInSeconds() != null) {
      apiTokenEntity.setExpiresAt(
          System.currentTimeMillis() + request.getExpiresInSeconds() * 1000);
    }
    apiTokenEntity.setUpdatedBy(updatedBy);
    apiTokenEntity.setUpdatedAt(DateTime.nowInUTC());
    apiTokenEntity = apiTokenRepository.save(apiTokenEntity);
    return UserTokenMapper.INSTANCE.toToken(apiTokenEntity);
  }
}
