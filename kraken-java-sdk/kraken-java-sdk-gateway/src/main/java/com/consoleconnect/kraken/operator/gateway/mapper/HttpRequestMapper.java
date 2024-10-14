package com.consoleconnect.kraken.operator.gateway.mapper;

import com.consoleconnect.kraken.operator.gateway.dto.HttpRequest;
import com.consoleconnect.kraken.operator.gateway.dto.SimpleHttpRequestDto;
import com.consoleconnect.kraken.operator.gateway.entity.HttpRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface HttpRequestMapper {

  HttpRequestMapper INSTANCE = Mappers.getMapper(HttpRequestMapper.class);

  HttpRequestEntity map(HttpRequest request);

  HttpRequest map(HttpRequestEntity entity);

  SimpleHttpRequestDto mapToDto(HttpRequestEntity entity);
}
