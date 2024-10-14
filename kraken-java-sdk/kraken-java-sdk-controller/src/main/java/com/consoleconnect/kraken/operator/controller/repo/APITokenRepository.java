package com.consoleconnect.kraken.operator.controller.repo;

import com.consoleconnect.kraken.operator.auth.entity.UserTokenEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface APITokenRepository
    extends PagingAndSortingRepository<UserTokenEntity, UUID>,
        JpaRepository<UserTokenEntity, UUID> {

  @Query(
      value =
          """
                    select * from kraken_user_token
                    where token_type = :tokenType
                    and revoked = :revoked
                    and expires_at > :expiresAtAfter
                    and max_life_end_at > :expiresAtAfter
                    and (:envId is null or claims->>'envId' = :envId)
                    and (:productId is null or claims->>'productId' = :productId)
                  """,
      nativeQuery = true)
  @Transactional(readOnly = true)
  Page<UserTokenEntity> search(
      @Param("tokenType") String tokenType,
      @Param("revoked") boolean revoked,
      @Param("expiresAtAfter") long expiresAtAfter,
      @Param("envId") String envId,
      @Param("productId") String productId,
      Pageable pageable);
}
