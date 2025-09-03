package com.consoleconnect.kraken.operator.auth.repo;

import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository
    extends PagingAndSortingRepository<UserEntity, UUID>, CrudRepository<UserEntity, UUID> {
  Optional<UserEntity> findOneByEmail(String email);

  @Query(
      value =
          "select e from #{#entityName} e "
                + " where  ( (:q) is null or LOWER(e.name) like %:q% or e.name like %:q% or LOWER(e.email) like %:q% )"
              + " and ( (:filterRoles) is null or e.role not in :filterRoles )"
              + "  and  ((:state) is null or  e.state = :state)"
              + "  and  ((:role) is null or  e.role = :role)")
  @Transactional(readOnly = true)
  Page<UserEntity> search(
      String q, Pageable pageable, List<String> filterRoles, UserStateEnum state, String role);
}
