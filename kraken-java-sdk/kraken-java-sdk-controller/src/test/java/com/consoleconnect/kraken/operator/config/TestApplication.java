package com.consoleconnect.kraken.operator.config;

import static com.consoleconnect.kraken.operator.config.TestContextConstants.PRODUCT_ID;

import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.consoleconnect.kraken")
@EnableJpaRepositories(basePackages = "com.consoleconnect.kraken")
@EntityScan(basePackages = "com.consoleconnect.kraken")
public class TestApplication {
  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  public static final String ENV_NAME = "stage";
  public static final String ENV_PRODUCTION_NAME = "production";
  public static String envId;
  public static String productionEnvId;

  @Autowired private EnvironmentService environmentService;

  @EventListener(PlatformSettingCompletedEvent.class)
  public void initialize() {
    if (envId == null) {
      envId =
          environmentService
              .search(PRODUCT_ID, UnifiedAssetService.getSearchPageRequest(0, Integer.MAX_VALUE))
              .getData()
              .stream()
              .filter(environment -> environment.getName().equalsIgnoreCase(ENV_NAME))
              .findFirst()
              .map(Environment::getId)
              .orElseGet(
                  () -> {
                    CreateEnvRequest createEnvRequest = new CreateEnvRequest();
                    createEnvRequest.setName(ENV_NAME);
                    return environmentService.create(PRODUCT_ID, createEnvRequest, null).getId();
                  });
    }
    if (productionEnvId == null) {
      productionEnvId =
          environmentService
              .search(PRODUCT_ID, UnifiedAssetService.getSearchPageRequest(0, Integer.MAX_VALUE))
              .getData()
              .stream()
              .filter(environment -> environment.getName().equalsIgnoreCase(ENV_PRODUCTION_NAME))
              .findFirst()
              .map(Environment::getId)
              .orElseGet(
                  () -> {
                    CreateEnvRequest createEnvRequest = new CreateEnvRequest();
                    createEnvRequest.setName(ENV_PRODUCTION_NAME);
                    return environmentService.create(PRODUCT_ID, createEnvRequest, null).getId();
                  });
    }
  }

  public static APIToken createAccessToken(APITokenService apiTokenService) {
    return createAccessToken(apiTokenService, PRODUCT_ID, envId);
  }

  public static APIToken createAccessToken(
      APITokenService apiTokenService, String productId, String envId) {
    CreateAPITokenRequest body = new CreateAPITokenRequest();
    body.setName("test-token-" + System.currentTimeMillis());
    body.setEnvId(envId);
    return apiTokenService.createToken(productId, body, System.currentTimeMillis() + "");
  }
}
