package com.consoleconnect.kraken.operator.core.config.jpa;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnBean(AppProperty.DualVersionConfig.class)
public class KrakenCustomNamingStrategy extends PhysicalNamingStrategyStandardImpl {

  private final transient AppProperty.DualVersionConfig dualVersionConfig;

  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    String canonicalName = name.getCanonicalName();
    Map<String, String> tableMaps = dualVersionConfig.getTableMaps();

    if (tableMaps.containsKey(canonicalName)) {
      return new Identifier(tableMaps.getOrDefault(canonicalName, canonicalName), name.isQuoted());
    }
    return super.toPhysicalTableName(name, context);
  }
}
