package com.consoleconnect.kraken.operator.core.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestPropertySource(properties = "app.dual-version-config.enabled=true")
class KrakenCustomHibernatePropertiesCustomizerTest extends AbstractIntegrationTest {
  public static final String KRAKEN_ASSET_V_2 = "kraken_asset_v2";
  @Autowired EntityManager entityManager;

  @Test
  @Order(1)
  void givenDualVersionEnabled_whenStart_thenV2TablesExist() {
    String query = "SELECT COUNT(*) FROM information_schema.tables WHERE  table_name = :tableName";
    Query nativeQuery = entityManager.createNativeQuery(query);
    nativeQuery.setParameter("tableName", KRAKEN_ASSET_V_2);
    Number result = (Number) nativeQuery.getSingleResult();
    assertTrue(result.intValue() > 0, "Table does not exist!");
  }
}
