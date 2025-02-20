package com.consoleconnect.kraken.operator.core.mapper;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface FacetsMapper {

  FacetsMapper INSTANCE = Mappers.getMapper(FacetsMapper.class);

  @BeanMapping(
      ignoreByDefault = true,
      ignoreUnmappedSourceProperties = {})
  @Mapping(target = "target", source = "target")
  @Mapping(target = "targetLocation", source = "targetLocation")
  @Mapping(target = "targetType", source = "targetType")
  @Mapping(target = "sourceValues", source = "sourceValues")
  @Mapping(target = "targetValues", source = "targetValues")
  @Mapping(target = "sourceValues", source = "sourceValues")
  @Mapping(target = "valueMapping", source = "valueMapping")
  void toRequestMapper(
      ComponentAPITargetFacets.Mapper mapper,
      @MappingTarget ComponentAPITargetFacets.Mapper mapperTarget);

  @BeanMapping(
      ignoreByDefault = true,
      ignoreUnmappedSourceProperties = {})
  @Mapping(target = "source", source = "source")
  @Mapping(target = "sourceLocation", source = "sourceLocation")
  @Mapping(target = "sourceValues", source = "sourceValues")
  @Mapping(target = "valueMapping", source = "valueMapping")
  void toResponseMapper(
      ComponentAPITargetFacets.Mapper mapper,
      @MappingTarget ComponentAPITargetFacets.Mapper mapperTarget);

  @BeanMapping(
      ignoreByDefault = true,
      ignoreUnmappedSourceProperties = {})
  @Mapping(target = "key", source = "key")
  @Mapping(target = "path", source = "path")
  @Mapping(target = "serverKey", source = "serverKey")
  @Mapping(target = "pathReferId", source = "pathReferId")
  @Mapping(target = "method", source = "method")
  void toEndpoint(
      ComponentAPITargetFacets.Endpoint endpoint,
      @MappingTarget ComponentAPITargetFacets.Endpoint endpointTarget);
}
