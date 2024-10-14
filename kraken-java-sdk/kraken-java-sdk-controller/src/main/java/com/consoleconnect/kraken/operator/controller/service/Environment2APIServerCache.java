package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.APIServerEnvDTO;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class Environment2APIServerCache {
  public static final String KEY_ENV_API_SERVER = "ENV_API_SERVER";
  private final LoadingCache<String, List<APIServerEnvDTO>> cache;
  private final EnvironmentRepository environmentRepository;

  public Environment2APIServerCache(
      ComponentAPIServerService apiServerService, EnvironmentRepository environmentRepository) {
    this.environmentRepository = environmentRepository;
    CacheLoader<String, List<APIServerEnvDTO>> loader;
    loader =
        new CacheLoader<>() {
          @Override
          public List<APIServerEnvDTO> load(String key) {
            return apiServerService.buildAPIServerCache();
          }
        };
    cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(20)).build(loader);
  }

  @SneakyThrows
  public List<APIServerEnvDTO> getAPIServerEnvs(String envId) {
    Optional<EnvironmentEntity> environment =
        environmentRepository.findById(UUID.fromString(envId));
    if (environment.isPresent()) {
      return cache.get(KEY_ENV_API_SERVER).stream()
          .filter(t -> t.getEnvName().equalsIgnoreCase(environment.get().getName()))
          .toList();
    } else {
      return new ArrayList<>();
    }
  }
}
