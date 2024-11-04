package com.consoleconnect.kraken.operator.controller;

import static com.consoleconnect.kraken.operator.toolkit.TestConstant.MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE;

import com.consoleconnect.kraken.operator.controller.dto.ClientMapperVersionPayloadDto;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClientMapperVersionCreator {
  @Autowired EnvironmentClientRepository environmentClientRepository;

  @Transactional
  public void newClientMapperVersion(String tagId, String envId) {
    Optional<EnvironmentClientEntity> oneByEnvIdAndClientKeyAndKind =
        environmentClientRepository.findOneByEnvIdAndClientKeyAndKind(
            envId,
            MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE,
            ClientEventTypeEnum.CLIENT_MAPPER_VERSION.name());
    if (oneByEnvIdAndClientKeyAndKind.isPresent()) {
      return;
    }
    EnvironmentClientEntity environmentClientEntity = new EnvironmentClientEntity();
    environmentClientEntity.setKind(ClientEventTypeEnum.CLIENT_MAPPER_VERSION.name());
    environmentClientEntity.setClientKey(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    environmentClientEntity.setEnvId(envId);
    environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
    ClientMapperVersionPayloadDto payload = new ClientMapperVersionPayloadDto();
    payload.setTagId(tagId);
    payload.setMapperKey(MEF_SONATA_API_TARGET_MAPPER_ADDRESS_RETRIEVE);
    environmentClientEntity.setPayload(payload);
    environmentClientRepository.save(environmentClientEntity);
  }
}
