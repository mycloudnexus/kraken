package com.consoleconnect.kraken.operator.auth.mapper;

import com.consoleconnect.kraken.operator.auth.entity.UserTokenEntity;
import com.consoleconnect.kraken.operator.auth.model.UserToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface UserTokenMapper {
  UserTokenMapper INSTANCE = Mappers.getMapper(UserTokenMapper.class);

  @Mapping(target = "token", ignore = true)
  UserToken toToken(UserTokenEntity entity);
}
