package com.consoleconnect.kraken.operator.core.config.jpa;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hibernate.cfg.MappingSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnBean(AppProperty.DualVersionConfig.class)
public class KrakenCustomHibernatePropertiesCustomizer implements HibernatePropertiesCustomizer {

  private final KrakenCustomNamingStrategy customNamingStrategy;
  private final KrakenCustomSpringImplicitNamingStrategy customSpringImplicitNamingStrategy;

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(MappingSettings.PHYSICAL_NAMING_STRATEGY, customNamingStrategy);
    hibernateProperties.put(
        MappingSettings.IMPLICIT_NAMING_STRATEGY, customSpringImplicitNamingStrategy);
  }
}
