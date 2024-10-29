package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.AppDemoProperty;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.controller.service.upgrade.ClasspathSourceUpgradeService;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DemoEnvironmentInitializer {
  public DemoEnvironmentInitializer(
      ResourceLoaderFactory loaderFactory,
      DataIngestionJob dataIngestionJob,
      ClasspathSourceUpgradeService classpathSourceUpgradeService,
      EnvironmentRepository environmentRepository,
      AppDemoProperty appDemoProperty) {
    this.loaderFactory = loaderFactory;
    this.dataIngestionJob = dataIngestionJob;
    this.classpathSourceUpgradeService = classpathSourceUpgradeService;
    this.environmentRepository = environmentRepository;
    this.appDemoProperty = appDemoProperty;
  }

  private final ResourceLoaderFactory loaderFactory;
  private final DataIngestionJob dataIngestionJob;
  private final ClasspathSourceUpgradeService classpathSourceUpgradeService;
  private final EnvironmentRepository environmentRepository;
  private final AppDemoProperty appDemoProperty;
  private volatile boolean initialized = false;

  @EventListener(classes = PlatformSettingCompletedEvent.class)
  public void prepareSampleConfig(PlatformSettingCompletedEvent event) {
    if (initialized || !appDemoProperty.isEnabled()) {
      return;
    }
    installMockAsset();
    initialized = true;
  }

  private void installMockAsset() {
    UnifiedAsset productAsset = classpathSourceUpgradeService.getProductAsset();
    ProductFacets productFacets = UnifiedAsset.getFacets(productAsset, ProductFacets.class);
    Optional.ofNullable(productFacets.getSampleConfigPaths()).stream()
        .flatMap(List::stream)
        .forEach(
            fullPath ->
                loaderFactory
                    .readFile(fullPath)
                    .ifPresent(
                        contentDescriptor -> {
                          contentDescriptor.setContent(
                              renderVariable(contentDescriptor.getContent(), loadContext()));
                          UnifiedAsset unifiedAsset =
                              YamlToolkit.parseYaml(
                                      contentDescriptor.getContent(), UnifiedAsset.class)
                                  .orElse(null);
                          dataIngestionJob.ingestData(
                              new IngestDataEvent(
                                  productAsset.getMetadata().getKey(),
                                  ResourceLoaderTypeEnum.RAW.getKind()
                                      + JsonToolkit.toJson(unifiedAsset),
                                  true,
                                  null,
                                  false,
                                  null));
                        }));
  }

  private String renderVariable(String content, Map<String, String> context) {

    StringBuilder stringBuilder = new StringBuilder(content);
    context.entrySet().stream()
        .forEach(
            entry -> {
              String newContent =
                  StringUtils.replace(
                      stringBuilder.toString(),
                      String.format("{{%s}}", entry.getKey()),
                      entry.getValue());
              stringBuilder.delete(0, stringBuilder.length());
              stringBuilder.append(newContent);
            });
    return stringBuilder.toString();
  }

  private Map<String, String> loadContext() {
    Map<String, String> context = new HashMap<>();
    String envId =
        environmentRepository.findAll().stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(EnvNameEnum.STAGE.name()))
            .findFirst()
            .map(t -> t.getId().toString())
            .orElse("");
    context.put("envId", envId);
    context.put("buyerId", appDemoProperty.getDefaultBuyerId());
    return context;
  }
}
