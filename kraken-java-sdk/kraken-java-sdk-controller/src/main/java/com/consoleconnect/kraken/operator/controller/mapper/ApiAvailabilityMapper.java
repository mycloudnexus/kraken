package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.controller.entity.ApiAvailabilityChangeHistoryEntity;
import com.consoleconnect.kraken.operator.controller.model.ApiAvailabilityChangeHistory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface ApiAvailabilityMapper {
  ApiAvailabilityMapper INSTANCE = Mappers.getMapper(ApiAvailabilityMapper.class);

  ApiAvailabilityChangeHistory toChangeHistory(ApiAvailabilityChangeHistoryEntity entity);
}
