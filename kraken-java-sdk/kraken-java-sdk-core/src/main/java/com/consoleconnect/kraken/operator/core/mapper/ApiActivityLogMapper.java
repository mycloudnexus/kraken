package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface ApiActivityLogMapper {

  ApiActivityLogMapper INSTANCE = Mappers.getMapper(ApiActivityLogMapper.class);

  ApiActivityLogEntity mapOnlySelf(ApiActivityLog request);

  default ApiActivityLogEntity map(ApiActivityLog request) {
    var entity = INSTANCE.mapOnlySelf(request);
    if (request.getRequest() != null || request.getResponse() != null) {
      entity.setApiLogBodyEntity(ApiActivityLogBodyMapper.INSTANCE.map(request));
    }
    return entity;
  }

  ApiActivityLog mapOnlySelf(ApiActivityLogEntity entity);

  default ApiActivityLog map(ApiActivityLogEntity entity) {
    var dto = INSTANCE.mapOnlySelf(entity);
    if (entity.getApiLogBodyEntity() != null) {
      dto.setRequest(entity.getApiLogBodyEntity().getRequest());
      dto.setResponse(entity.getApiLogBodyEntity().getResponse());
    }
    return dto;
  }
}
