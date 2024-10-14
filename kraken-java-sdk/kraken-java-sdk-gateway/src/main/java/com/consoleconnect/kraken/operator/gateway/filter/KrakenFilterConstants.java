package com.consoleconnect.kraken.operator.gateway.filter;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;

public abstract class KrakenFilterConstants {
  private KrakenFilterConstants() {}

  public static final String X_REQUEST_ID = "x-kraken-request-id";
  public static final String X_ENTITY_ID = "x-kraken-entity-id";
  public static final String X_EVENT_ENTITY_ID = "x-kraken-eventEntity-id";
  public static final String X_TRANSFORMED_ENTITY_ID = "x-kraken-transformed-entity-id";
  public static final String X_ENTITY = "x-kraken-entity";
  public static final String X_EVENT_ENTITY = "x-kraken-eventEntity";
  public static final String X_ORIGINAL_REQUEST_BODY =
      ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
  public static final String X_KRAKEN_RESPONSE_BODY = "x-kraken-response-body";
  public static final String X_KRAKEN_RENDERED_RESPONSE_BODY = "x-kraken-rendered-response-body";

  public static final String GATEWAY_SERVICE = "GATEWAY";
  public static final String X_LOG_ENTITY_ID = "x-kraken-log-entity-id";
  public static final String X_LOG_TRANSFORMED_ENTITY_ID = "x-kraken-log-transformed-entity-id";
  public static final String X_LOG_REQUEST_ID = "x-kraken-log-request-id";
  public static final String X_KRAKEN_LOG_CALL_SEQ = "x-kraken-log-call-seq";
  public static final String X_KRAKEN_TARGET_VALUE_MAPPER = "x-kraken-target-value-mapper";
  public static final String X_KRAKEN_BUYER_ID = "x-kraken-buyer-id";
  public static final String X_KRAKEN_AUTH_KEY = "x-kraken-key";
  public static final String X_KRAKEN_URL = "x-kraken-url";
}
