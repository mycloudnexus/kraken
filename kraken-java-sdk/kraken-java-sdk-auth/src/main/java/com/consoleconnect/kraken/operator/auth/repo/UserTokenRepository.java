package com.consoleconnect.kraken.operator.auth.repo;

import com.consoleconnect.kraken.operator.auth.entity.UserTokenEntity;
import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserTokenRepository
    extends PagingAndSortingRepository<UserTokenEntity, UUID>,
        JpaRepository<UserTokenEntity, UUID> {

  Optional<UserTokenEntity> findOneByToken(String token);

  Page<UserTokenEntity> findAllByUserIdAndTokenTypeAndRevokedAndExpiresAtAfterAndMaxLifeEndAtAfter(
      String userId,
      UserTokenTypeEnum tokenTypeEnum,
      boolean revoked,
      long expiresAt,
      long maxLifeEndAt,
      Pageable pageable);
}
