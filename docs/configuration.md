# Configuration

## Generic Configuration for applications

| Key | Value |
| --- | ----- |
| `server.port` | `${port:8000}` |
| `spring.main.web-application-type` | `reactive` |
| `spring.codec.max-in-memory-size` | `500KB` |
| `spring.cloud.gateway.httpclient.connect-timeout` | `20000` |
| `spring.cloud.gateway.httpclient.pool.max-connections` | `1500` |
| `spring.cloud.gateway.httpclient.pool.type` | `FIXED` |
| `spring.application.name` | `@project.artifactId@` |
| `spring.application.description` | `@project.name@` |
| `spring.build.version` | `@project.version@` |
| `spring.jackson.default-property-inclusion` | `NON_NULL` |
| `spring.jackson.deserialization.fail-on-unknown-properties` | `false` |
| `spring.datasource.driverClassName` | `org.postgresql.Driver` |
| `spring.datasource.password` | `password` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/kraken` |
| `spring.datasource.username` | `postgresql` |
| `spring.datasource.hikari.pool-name` | `hikari-platform-service-platform` |
| `spring.datasource.hikari.connection-test-query` | `SELECT 1` |
| `spring.datasource.hikari.maximum-pool-size` | `50` |
| `spring.datasource.hikari.max-lifetime` | `600000` |
| `spring.datasource.hikari.connection-timeout` | `60000` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `spring.jpa.show-sql` | `false` |
| `spring.jpa.database` | `POSTGRESQL` |
| `spring.jpa.properties.hibernate.dialect` | `org.hibernate.dialect.PostgreSQLDialect` |
| `spring.jpa.properties.hibernate.jdbc.time_zone` | `UTC` |
| `spring.flyway.baseline-on-migrate` | `true` |
| `spring.flyway.enabled` | `false` |
| `management.endpoint.health.show-details` | `ALWAYS` |
| `management.endpoint.gateway.enabled` | `false` |
| `management.endpoints.web.base-path` | `/actuator` |
| `management.endpoints.web.exposure.include` | `health,info,prometheus` |
| `info.app.encoding` | `@project.build.sourceEncoding@` |
| `info.app.java.source` | `@java.version@` |
| `info.app.java.target` | `@java.version@` |
| `springdoc.show-actuator` | `true` |
| `springdoc.api-docs.enabled` | `true` |
| `springdoc.api-docs.path` | `/v3/api-docs` |
| `springdoc.swagger-ui.enabled` | `true` |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| `springdoc.swagger-ui.supported-submit-methods` | `["get", "post", "patch", "delete"]` |
| `springdoc.swagger-ui.config-url` | `/v3/api-docs/swagger-config` |
| `springdoc.swagger-ui.urls[0].url` | `/v3/api-docs` |
| `springdoc.swagger-ui.urls[0].name` | `Kraken Hub` |
| `springdoc.servers[0].url` | `http://localhost:8000` |
| `springdoc.servers[0].description` | `localhost` |
| `logging.level.com.consoleconnect.kraken` | `DEBUG` |
| `logging.level.reactor.netty` | `DEBUG` |
| `logging.level.org.springframework.cloud.gateway` | `DEBUG` |

## kraken-app-controller

## assets

| Key | Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.initialize-exclude-assets` | `classpath:/mef-sonata/apis/api.product.offering.yaml`<br>`classpath:/mef-sonata/api-targets/api-target.quote.eline.add.yaml`<br>`classpath:/mef-sonata/api-targets/api-target.quote.eline.read.yaml`<br>`classpath:/mef-sonata/api-targets/api-target.quote.uni.add.yaml`<br>`classpath:/mef-sonata/api-targets/api-target.quote.uni.read.yaml`<br>`classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.eline.add.yaml`<br>`classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.uni.add.yaml`<br>`classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.uni.read.yaml`<br>`classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.eline.read.yaml` | No | List of assets to exclude from initialization. |
| `app.query-exclude-asset-kinds` | `kraken.component.api`<br>`kraken.component.api-spec` | No | List of asset kinds to exclude from queries. |
| `app.query-exclude-asset-keys` | `mef.sonata.api.poq`<br>`mef.sonata.api-spec.product.offering.qualification`<br>`mef.sonata.api-target-mapper.quote.eline.add`<br>`mef.sonata.api-target-mapper.quote.uni.add`<br>`mef.sonata.api-target-mapper.quote.uni.read`<br>`mef.sonata.api-target-mapper.quote.eline.read` | No | List of asset keys to exclude from queries. |

This table details the configuration options related to asset exclusion for initialization and queries, including their values, whether they are required, and their descriptions.

Example:
```
app:
  initialize-exclude-assets:
    - classpath:/mef-sonata/apis/api.product.offering.yaml
    - classpath:/mef-sonata/api-targets/api-target.quote.eline.add.yaml
    - classpath:/mef-sonata/api-targets/api-target.quote.eline.read.yaml
    - classpath:/mef-sonata/api-targets/api-target.quote.uni.add.yaml
    - classpath:/mef-sonata/api-targets/api-target.quote.uni.read.yaml
    - classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.eline.add.yaml
    - classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.uni.add.yaml
    - classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.uni.read.yaml
    - classpath:/mef-sonata/api-targets-mappers/api-target-mapper.quote.eline.read.yaml

  query-exclude-asset-kinds:
    - kraken.component.api
    - kraken.component.api-spec
  query-exclude-asset-keys:
    - mef.sonata.api.poq
    - mef.sonata.api-spec.product.offering.qualification
    - mef.sonata.api-target-mapper.quote.eline.add
    - mef.sonata.api-target-mapper.quote.uni.add
    - mef.sonata.api-target-mapper.quote.uni.read
    - mef.sonata.api-target-mapper.quote.eline.read
```

### Auth
Here's the YAML configuration converted into a markdown table with the additional "Required" and "Description" columns:

### Configuration Table

| Key | Default Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.security.login.enabled` | `false` | Yes | Indicates if the login security is enabled. |
| `app.security.login.hmac-secret` | `NULL` | Yes | The HMAC secret used for login authentication. |
| `app.security.login.user-list[0].id` | `NULL` | Yes | The ID of the user. |
| `app.security.login.user-list[0].user-name` | `NULL` | Yes | The username of the user. |
| `app.security.login.user-list[0].role` | `USER` | NO | The role assigned to the user. |
| `app.security.login.user-list[0].password` | `NULL` | Yes | The hashed password for the user. |
| `app.security.jwt.issuer` | `NULL` | Yes | The issuer URL for the JWT. |
| `app.security.jwt.key-id` | `NULL` | Yes | The key ID used to identify the JWT signing key. |
| `app.security.jwt.secret` | `NULL` | Yes | The secret used for JWT signing and validation. |
| `app.security.resource-server.enabled` | `false` | Yes | Indicates if the resource server security is enabled. |
| `app.security.resource-server.jwt[0].issuer` | `NULL` | Yes | The issuer URL for the JWT. |
| `app.security.resource-server.jwt[0].key-id` | `NULL` | Yes | The key ID used to identify the JWT signing key. |
| `app.security.resource-server.jwt[0].secret` | `NULL` | Yes | The secret used for JWT signing and validation. |

Example:
```
app:
  security:
    login:
      enabled: true
      hmac-secret: 1234567
      user-list:
        - id: admin
          user-name: admin
          role: ADMIN
          password: <admin>
      jwt:
        issuer: https://kraken.consoleconnect.com/issuer
        key-id: kraken
        secret: <scret>
    resource-server:
      enabled: true
      jwt:
        - issuer: https://kraken.consoleconnect.com/issuer
          key-id: kraken
          secret: <secret>
```

## kraken-app-hub

### Scheduler Tasks

Hub need to talk with `kraken-app-agent` to report heartbeat

| Key | Default Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.task.run-config.heartBeat.run-flag` | `true` | Yes | Indicates if the heartbeat task should run. |
| `app.task.run-config.heartBeat.fixed-delay` | `5000` | Yes | Delay between heartbeat tasks in milliseconds. |
| `app.task.run-config.heartBeat.initial-delay` | `20000` | Yes | Initial delay before the first heartbeat task in milliseconds. |
| `app.task.run-config.scheduleCheckDeployment.run-flag` | `true` | Yes | Indicates if the deployment check task should run. |
| `app.task.run-config.scheduleCheckDeployment.fixed-delay` | `20000` | Yes | Delay between deployment check tasks in milliseconds. |
| `app.task.run-config.scheduleCheckDeployment.initial-delay` | `50000` | Yes | Initial delay before the first deployment check task in milliseconds. |
| `app.task.enabled` | `true` | Yes | Indicates if the task configurations are enabled. |
| `app.agent.endpoint` | `http://localhost:8002` | false | The endpoint URL for the agent. |

Example:

```
app:
  task:
    run-config:
      heartBeat:
        run-flag: true
        fixed-delay: 5000
        initial-delay: 20000
      scheduleCheckDeployment:
        run-flag: true
        fixed-delay: 20000
        initial-delay: 50000
    enabled: true
  agent:
    endpoint: http://localhost:8002

```

### Auth 

Configuration is auth is required to access the APIs. ( if auth enabled, the access-token need to be geneated via `kraken-app-controller`)

| Key | Default Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.security.resource-server.enabled` | `false` | NO | Indicates if the resource server security is enabled. |
| `app.security.resource-server.jwt[0].issuer` | `NULL` | Yes | The issuer URL for the JWT. |
| `app.security.resource-server.jwt[0].key-id` | `NULL` | Yes | The key ID used to identify the JWT signing key. |
| `app.security.resource-server.jwt[0].secret` | `NULL` | Yes | The secret used for JWT signing and validation. |
| `app.security.resource-server.bearer-token-header-name` | `Authorization` | NO | The name of the HTTP header used for bearer tokens. |

Exmaple:
```
app:
  security:
    resource-server:
      enabled: true
      jwt:
        - issuer: https://app.consoleconnect.com/kraken/issuer
          key-id: kraken
          secret: <secret key>
      bearer-token-header-name: x-kraken-key
```

## app-agent

### DB configuration

Agent MUST point to the same database as Hub, and the ddl-auto MUST be configured to `none`

```
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

### Control Plane Server

Agent need to talk with `kraken-app-controller` to pull and upload data.

| Key | Default Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.controlPlane.url` | `NULL` | Yes | The base URL for the control plane. |
| `app.controlPlane.token` | `NULL` | Yes | The authentication token used for API requests. |
| `app.controlPlane.uploadEndpoint` | `/v2/callback/audits/api-activities` | NO | The endpoint for uploading API activity data. |
| `app.controlPlane.retrieveProductReleaseDetailEndpoint` | `/v2/callback/audits/releases/{releaseId}/components` | NO | The endpoint for retrieving details about product releases. |
| `app.controlPlane.reportClientReloadStatusEndpoint` | `/v2/callback/audits/client-event-report` | NO | The endpoint for reporting client reload status. |
| `app.controlPlane.latestDeploymentEndpoint` | `/v2/callback/audits/deployments/latest` | NO | The endpoint for retrieving the latest deployment information. |
| `app.controlPlane.apiServerEndpoint` | `/v2/callback/audits/api-servers` | NO | The endpoint for interacting with API servers. |
| `app.controlPlane.syncFromServerEndpoint` | `/v2/callback/audits/sync-server-asset` | NO | The endpoint for synchronizing server assets. |

Example:
```
app:
  controlPlane:
    url: http://localhost:8001
    token: Token kraken_1nOPAyDNuC6umD9ZcUWLfPpjIYQETPPyRZ4neWC0SvoR6NXls_1584608469
    uploadEndpoint: /v2/callback/audits/api-activities
    retrieveProductReleaseDetailEndpoint: /v2/callback/audits/releases/{releaseId}/components
    reportClientReloadStatusEndpoint: /v2/callback/audits/client-event-report
    latestDeploymentEndpoint: /v2/callback/audits/deployments/latest
    apiServerEndpoint: /v2/callback/audits/api-servers
    syncFromServerEndpoint: /v2/callback/audits/sync-server-asset
```

### Scheduler Tasks

| Key | Default Value | Required | Description |
| --- | ----- | -------- | ----------- |
| `app.task.run-config.SynApiActivityLog.run-flag` | `false` | Yes | Indicates if the SynApiActivityLog task should run. |
| `app.task.run-config.SynApiActivityLog.fixed-delay` | `15000` | Yes | Delay between SynApiActivityLog tasks in milliseconds. |
| `app.task.run-config.SynApiActivityLog.initial-delay` | `30000` | Yes | Initial delay before the first SynApiActivityLog task in milliseconds. |
| `app.task.run-config.heartBeat.run-flag` | `false` | Yes | Indicates if the heartbeat task should run. |
| `app.task.run-config.heartBeat.fixed-delay` | `5000` | Yes | Delay between heartbeat tasks in milliseconds. |
| `app.task.run-config.heartBeat.initial-delay` | `20000` | Yes | Initial delay before the first heartbeat task in milliseconds. |
| `app.task.run-config.synApiServerInfo.run-flag` | `false` | Yes | Indicates if the synApiServerInfo task should run. |
| `app.task.run-config.synApiServerInfo.fixed-delay` | `10000` | Yes | Delay between synApiServerInfo tasks in milliseconds. |
| `app.task.run-config.synApiServerInfo.initial-delay` | `30000` | Yes | Initial delay before the first synApiServerInfo task in milliseconds. |
| `app.task.run-config.syncServerAssets.run-flag` | `false` | Yes | Indicates if the syncServerAssets task should run. |
| `app.task.run-config.syncServerAssets.fixed-delay` | `60000` | Yes | Delay between syncServerAssets tasks in milliseconds. |
| `app.task.run-config.syncServerAssets.initial-delay` | `50000` | Yes | Initial delay before the first syncServerAssets task in milliseconds. |
| `app.task.run-config.scheduledCheckLatestProductRelease.run-flag` | `true` | Yes | Indicates if the scheduledCheckLatestProductRelease task should run. |
| `app.task.run-config.scheduledCheckLatestProductRelease.fixed-delay` | `20000` | Yes | Delay between scheduledCheckLatestProductRelease tasks in milliseconds. |
| `app.task.run-config.scheduledCheckLatestProductRelease.initial-delay` | `50000` | Yes | Initial delay before the first scheduledCheckLatestProductRelease task in milliseconds. |

Example: 
```
app:
  task:
    run-config:
      SynApiActivityLog:
        run-flag: false
        fixed-delay: 15000
        initial-delay: 30000
      heartBeat:
        run-flag: false
        fixed-delay: 5000
        initial-delay: 20000
      synApiServerInfo:
        run-flag: false
        fixed-delay: 10000
        initial-delay: 30000
      syncServerAssets:
        run-flag: false
        fixed-delay: 60000
        initial-delay: 50000
      scheduledCheckLatestProductRelease:
        run-flag: true
        fixed-delay: 20000
        initial-delay: 50000
```