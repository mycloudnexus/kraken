package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface ApiActivityLogBodyMapper {

  ApiActivityLogBodyMapper INSTANCE = Mappers.getMapper(ApiActivityLogBodyMapper.class);

  ApiActivityLogBodyEntity map(ApiActivityLog request);

  ApiActivityLog map(ApiActivityLogBodyEntity entity);
}
