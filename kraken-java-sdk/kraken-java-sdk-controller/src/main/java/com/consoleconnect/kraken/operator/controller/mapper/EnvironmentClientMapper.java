package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.controller.model.EnvironmentClient;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface EnvironmentClientMapper {
  EnvironmentClientMapper INSTANCE = Mappers.getMapper(EnvironmentClientMapper.class);

  EnvironmentClient map(EnvironmentClientEntity entity);
}
