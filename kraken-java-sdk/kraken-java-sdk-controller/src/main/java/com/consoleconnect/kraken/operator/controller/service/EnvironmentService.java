package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.mapper.EnvironmentMapper;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class EnvironmentService {
  private final EnvironmentRepository environmentRepository;
  private final UnifiedAssetService unifiedAssetService;

  public Paging<Environment> search(String productIdOrKey, Pageable pageable) {
    String productId = null;
    try {
      productId = unifiedAssetService.findOneByIdOrKey(productIdOrKey).getKey();
    } catch (KrakenException ex) {
      log.info("Product not found, {}", productIdOrKey);
      return PagingHelper.toPaging(Page.empty(), EnvironmentMapper.INSTANCE::toEnv);
    }
    Page<EnvironmentEntity> data = environmentRepository.findAllByProductId(productId, pageable);
    return PagingHelper.toPaging(data, EnvironmentMapper.INSTANCE::toEnv);
  }

  @Transactional(readOnly = true)
  public Environment findOne(String id) {
    return EnvironmentMapper.INSTANCE.toEnv(
        environmentRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> KrakenException.notFound("Environment not found")));
  }

  @Transactional
  public Environment create(String productIdOrKey, CreateEnvRequest request, String createdBy) {
    String productId = unifiedAssetService.findOneByIdOrKey(productIdOrKey).getKey();
    EnvironmentEntity environmentEntity = new EnvironmentEntity();
    environmentEntity.setName(request.getName());
    environmentEntity.setProductId(productId);
    environmentEntity.setCreatedBy(createdBy);
    environmentEntity = environmentRepository.save(environmentEntity);
    return EnvironmentMapper.INSTANCE.toEnv(environmentEntity);
  }

  @Transactional
  public List<Environment> findAll() {
    return environmentRepository.findAll().stream().map(EnvironmentMapper.INSTANCE::toEnv).toList();
  }
}
