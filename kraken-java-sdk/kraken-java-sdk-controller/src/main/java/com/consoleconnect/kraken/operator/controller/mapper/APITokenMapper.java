package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.auth.model.UserToken;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface APITokenMapper {
  APITokenMapper INSTANCE = Mappers.getMapper(APITokenMapper.class);

  APIToken toAPIToken(UserToken userToken);
}
