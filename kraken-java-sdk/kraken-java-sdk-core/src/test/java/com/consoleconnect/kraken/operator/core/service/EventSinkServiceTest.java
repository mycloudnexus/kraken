package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
public class EventSinkServiceTest extends AbstractIntegrationTest {
  public static final String MEF_SONATA_RELEASE_1_1_0 = "mef.sonata.release@1.1.0";
  @Autowired EventSinkService eventSinkService;
  @Autowired MgmtEventRepository mgmtEventRepository;

  @Test
  @Order(1)
  void givenTemplateUpgradeResult_whenReportTemplateUpgradeResult_thenOk() throws Exception {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind(),
            MEF_SONATA_RELEASE_1_1_0,
            MEF_SONATA_RELEASE_1_1_0);
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(LabelConstants.LABEL_RELEASE_ID, UUID.randomUUID().toString());
    unifiedAsset.getMetadata().setKey(MEF_SONATA_RELEASE_1_1_0);
    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    BeanUtils.copyProperties(unifiedAsset, unifiedAssetDto);
    eventSinkService.reportTemplateUpgradeResult(
        unifiedAssetDto,
        UpgradeResultEventEnum.UPGRADE,
        event -> {
          event.setEnvName(EnvNameEnum.STAGE);
          event.setUpgradeBeginAt(ZonedDateTime.now());
          event.setUpgradeEndAt(ZonedDateTime.now().plusSeconds(10));
        });
    List<MgmtEventEntity> mgmtEventEntities =
        eventSinkService.findByPage(
            List.of(MgmtEventType.TEMPLATE_UPGRADE_RESULT),
            EventStatusType.WAIT_TO_SEND,
            PageRequest.of(0, 10));
    MatcherAssert.assertThat(mgmtEventEntities, Matchers.hasSize(Matchers.greaterThanOrEqualTo(1)));
    mgmtEventRepository.deleteAll(mgmtEventEntities);
  }

  @Test
  @Order(2)
  void givenAppStartUp_whenReportKrakenVersion_thenOk() {
    eventSinkService.reportKrakenVersionUpgradeResult(
        EnvNameEnum.CONTROL_PLANE, "1.0.0", ZonedDateTime.now());
    List<MgmtEventEntity> krakenVersionEntities =
        eventSinkService.findByPage(
            List.of(MgmtEventType.CLIENT_APP_VERSION_UPGRADE_RESULT),
            EventStatusType.WAIT_TO_SEND,
            PageRequest.of(0, 10));
    MatcherAssert.assertThat(
        krakenVersionEntities, Matchers.hasSize(Matchers.greaterThanOrEqualTo(1)));
    mgmtEventRepository.deleteAll(krakenVersionEntities);
  }
}
