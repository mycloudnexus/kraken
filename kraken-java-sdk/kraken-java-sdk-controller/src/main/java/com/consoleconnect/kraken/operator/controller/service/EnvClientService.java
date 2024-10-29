package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.mapper.EnvironmentClientMapper;
import com.consoleconnect.kraken.operator.controller.model.EnvironmentClient;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EnvClientService {
  private final EnvironmentClientRepository environmentClientRepository;

  public Paging<EnvironmentClient> listClients(
      String envId, ClientReportTypeEnum reportType, Pageable page) {
    ZonedDateTime referDataTime = ZonedDateTime.now().minusMinutes(5);
    Page<EnvironmentClientEntity> clientEntities;
    if (StringUtils.isBlank(envId)) {
      clientEntities =
          environmentClientRepository.findAllByKindAndUpdatedAtGreaterThan(
              reportType.name(), referDataTime, page);
    } else {
      clientEntities =
          environmentClientRepository.findAllByEnvIdAndKindAndUpdatedAtGreaterThan(
              envId, reportType.name(), referDataTime, page);
    }

    return PagingHelper.toPaging(clientEntities, EnvironmentClientMapper.INSTANCE::map);
  }

  public Paging<EnvironmentClient> listClients4Reload(
      String envId, ClientReportTypeEnum reportType, Pageable page) {
    Page<EnvironmentClientEntity> clientEntities;
    clientEntities =
        environmentClientRepository.findTop10ByEnvIdAndKindOrderByUpdatedAtDesc(
            envId, reportType.name(), page);

    return PagingHelper.toPaging(clientEntities, EnvironmentClientMapper.INSTANCE::map);
  }
}
