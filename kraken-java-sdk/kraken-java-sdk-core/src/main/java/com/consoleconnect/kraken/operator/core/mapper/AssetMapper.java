package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.AssetLinkEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface AssetMapper {
  AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "facets", ignore = true)
  UnifiedAssetDto toAsset(UnifiedAssetEntity entity);

  AssetLinkDto toAssetLinkDto(AssetLinkEntity entity);

  Metadata toMetadata(UnifiedAssetEntity entity);
}
