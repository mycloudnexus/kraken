package com.consoleconnect.kraken.operator.controller.mapper;

import com.consoleconnect.kraken.operator.controller.dto.UnifiedAssetDetailsDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface AssetMapper {
  AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

  UnifiedAssetDetailsDto toDetails(UnifiedAssetDto asset);
}
