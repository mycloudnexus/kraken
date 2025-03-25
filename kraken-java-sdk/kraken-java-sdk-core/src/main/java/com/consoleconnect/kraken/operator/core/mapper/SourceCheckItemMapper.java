package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.dto.SourceCheckItem;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface SourceCheckItemMapper {

  SourceCheckItemMapper INSTANCE = Mappers.getMapper(SourceCheckItemMapper.class);

  @BeanMapping(
      ignoreByDefault = true,
      ignoreUnmappedSourceProperties = {})
  @Mapping(target = "sourceType", source = "sourceType")
  @Mapping(target = "discrete", source = "discrete")
  @Mapping(target = "sourceValues", source = "sourceValues")
  @Mapping(target = "target", source = "target")
  @Mapping(target = "allowValueLimit", source = "allowValueLimit")
  @Mapping(target = "systemValueLimit", source = "systemValueLimit")
  void toSourceCheckItem(ComponentAPITargetFacets.Mapper from, @MappingTarget SourceCheckItem to);
}
