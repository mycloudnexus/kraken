package com.consoleconnect.kraken.operator.controller.entity;

import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "kraken_mgmt_api_availability_change_history")
public class ApiAvailabilityChangeHistoryEntity extends AbstractEntity {
  private String action;
  private String mapperKey;
  private String version;
  private String env;
  private boolean available;
}
