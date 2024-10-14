package com.consoleconnect.kraken.operator.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.consoleconnect.kraken")
@EnableJpaRepositories(basePackages = "com.consoleconnect.kraken")
@EntityScan(basePackages = "com.consoleconnect.kraken")
public class CustomConfig implements ApplicationListener<WebServerInitializedEvent> {

  public static final String X_KRAKEN_KEY_TOKEN =
      "bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJidXllcjAyIiwicm9sZXMiOlsiUk9MRTpCVVlFUiJdLCJpYXQiOjE3MjEwOTc4NzgsImV4cCI6NDg3Njc3MTQ3OH0.FfjLQocliW4I-P8ti2fL2AzISGCILiqERbAxErHSd2Immax4MNejr5iOgRAwNWNH8zymx5waZvfJwsPGuN3vDfTfpqMF57ztWKqtiWl1WgBgTzowO4TzmIJnT-4bdrIb7_3e_eoQEJ0dNfTdBWCjSJFxwlOdeE3NswiGrjsl_ez88AEH0e42QPsAZO5wC4QFIekXbCus1zZcfzTfRfvch4FVDWJyOKmTbEIIfyGAPkGqh4kCVpXS_TYTJfG37duA3dIFIYZe0tG4ZiKkPIP41MfkzoKJ2ltmgtfJ_6KOplOhenWxzaRmRlRLtZKGNbar0PKU_2HIBhQLbrWuO6FTOg";
  int serverPort;

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {

    serverPort = event.getWebServer().getPort();
  }
}
