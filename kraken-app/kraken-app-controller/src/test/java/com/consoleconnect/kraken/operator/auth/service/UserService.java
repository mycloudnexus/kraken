package com.consoleconnect.kraken.operator.auth.service;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserRequest;
import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

@TestComponent
public class UserService {
  public Paging<User> search(String q, PageRequest pageRequest) {
    User user = new User();
    user.setName(q);
    user.setId(UUID.randomUUID().toString());
    return PagingHelper.toPage(
        List.of(user), pageRequest.getPageNumber(), pageRequest.getPageSize());
  }

  public User create(CreateUserRequest request, String createdBy) {
    Assert.isTrue(request != null, "request must not be null" + createdBy);
    return null;
  }

  public UserEntity findOneByIdOrEmail(String idOrEmail) {
    UserEntity userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID());
    userEntity.setEmail(idOrEmail);
    return userEntity;
  }
}
