package com.consoleconnect.kraken.operator.auth.repo;

import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository
    extends PagingAndSortingRepository<UserEntity, UUID>,
        CrudRepository<UserEntity, UUID>,
        JpaSpecificationExecutor<UserEntity> {
  Optional<UserEntity> findOneByEmail(String email);
}
