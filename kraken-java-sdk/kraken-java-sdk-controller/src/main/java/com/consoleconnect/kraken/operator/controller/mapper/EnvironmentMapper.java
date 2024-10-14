package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface EnvironmentMapper {
  EnvironmentMapper INSTANCE = Mappers.getMapper(EnvironmentMapper.class);

  Environment toEnv(EnvironmentEntity entity);
}
