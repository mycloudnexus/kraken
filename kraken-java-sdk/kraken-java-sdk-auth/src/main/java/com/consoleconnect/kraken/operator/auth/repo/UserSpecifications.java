package com.consoleconnect.kraken.operator.auth.repo;

import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
  private UserSpecifications() {}

  public static Specification<UserEntity> search(
      String q, List<String> filterRoles, UserStateEnum state, String role) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // case-insensitive search for name or email
      if (q != null && !q.isBlank()) {
        String pattern = "%" + q.toLowerCase() + "%";
        Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
        Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
        predicates.add(cb.or(nameMatch, emailMatch));
      }

      // exclude certain roles
      if (filterRoles != null && !filterRoles.isEmpty()) {
        predicates.add(cb.not(root.get("role").in(filterRoles)));
      }

      // exact state match
      if (state != null) {
        predicates.add(cb.equal(root.get("state"), state));
      }

      // exact role match
      if (role != null && !role.isBlank()) {
        predicates.add(cb.equal(root.get("role"), role));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
