package com.consoleconnect.kraken.operator.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "springdoc")
public class SwaggerConfig {

  private List<Server> servers;

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }

  // https://swagger.io/docs/specification/authentication/
  @Bean
  public OpenAPI customOpenAPI(@Autowired InfoEndpoint infoEndpoint) {
    Map<String, Object> build = (Map<String, Object>) infoEndpoint.info().get("build");
    if (build == null) {
      build = new HashMap<>();
      build.put("name", "N/A");
      build.put("version", "N/A");
    }
    OpenAPI api =
        new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(
                new Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme().type(Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .info(
                new Info()
                    .title((String) build.get("name"))
                    .version((String) build.get("version")));
    api.setServers(servers);
    return api;
  }
}
