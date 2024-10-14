package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface ApiActivityLogMapper {

  ApiActivityLogMapper INSTANCE = Mappers.getMapper(ApiActivityLogMapper.class);

  ApiActivityLogEntity map(ApiActivityLog request);

  ApiActivityLog map(ApiActivityLogEntity entity);
}
