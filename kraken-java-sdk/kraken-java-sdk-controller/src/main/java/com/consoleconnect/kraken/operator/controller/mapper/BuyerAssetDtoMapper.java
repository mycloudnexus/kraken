package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface BuyerAssetDtoMapper {

  BuyerAssetDtoMapper INSTANCE = Mappers.getMapper(BuyerAssetDtoMapper.class);

  BuyerAssetDto toBuyerAssetDto(UnifiedAssetDto unifiedAssetDto);
}
