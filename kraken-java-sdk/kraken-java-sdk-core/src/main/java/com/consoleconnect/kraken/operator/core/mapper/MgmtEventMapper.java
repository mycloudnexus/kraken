package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.entity.MgmtEventDto;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface MgmtEventMapper {
  MgmtEventMapper INSTANCE = Mappers.getMapper(MgmtEventMapper.class);

  MgmtEventDto map(MgmtEventEntity entity);
}
