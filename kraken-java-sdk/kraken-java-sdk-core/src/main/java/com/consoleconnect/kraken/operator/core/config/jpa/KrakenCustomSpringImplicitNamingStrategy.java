package com.consoleconnect.kraken.operator.core.config.jpa;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnBean(AppProperty.DualVersionConfig.class)
public class KrakenCustomSpringImplicitNamingStrategy extends SpringImplicitNamingStrategy {
  private final transient AppProperty.DualVersionConfig dualVersionConfig;

  @Override
  public Identifier determineIndexName(ImplicitIndexNameSource source) {
    Map<String, String> tableMaps = dualVersionConfig.getTableMaps();
    if (tableMaps.containsValue(source.getTableName().getCanonicalName())) {
      Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
      Map.Entry<String, String> entry =
          tableMaps.entrySet().stream()
              .filter(e -> e.getValue().equals(source.getTableName().getCanonicalName()))
              .findFirst()
              .orElseThrow();
      return toIdentifier(
          userProvidedIdentifier
              .getCanonicalName()
              .replace(entry.getKey(), source.getTableName().getCanonicalName()),
          source.getBuildingContext());
    }
    return super.determineIndexName(source);
  }
}
