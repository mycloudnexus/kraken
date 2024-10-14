package com.consoleconnect.kraken.operator.auth.mapper;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserRequest;
import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import com.consoleconnect.kraken.operator.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  User toUser(UserEntity entity);

  @Mapping(target = "password", ignore = true)
  UserEntity toEntity(CreateUserRequest entity);
}
