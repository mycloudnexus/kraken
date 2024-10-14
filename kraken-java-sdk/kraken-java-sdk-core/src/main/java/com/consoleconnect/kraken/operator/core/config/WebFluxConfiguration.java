package com.consoleconnect.kraken.operator.core.config;

import com.consoleconnect.kraken.operator.core.exception.KrakenExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

@Configuration
@AutoConfigureBefore(WebFluxAutoConfiguration.class)
public class WebFluxConfiguration implements WebFluxConfigurer {

  /** 500KB */
  private static final int BYTE_COUNT = 1024 * 1024 * 10;

  @Bean
  public WebProperties.Resources resources() {
    return new WebProperties.Resources();
  }

  @Bean
  @Order(-1)
  public ErrorWebExceptionHandler errorWebExceptionHandler(
      ErrorAttributes errorAttributes,
      WebProperties webProperties,
      ObjectProvider<ViewResolver> viewResolvers,
      ServerCodecConfigurer serverCodecConfigurer,
      ApplicationContext applicationContext,
      ApplicationEventPublisher publisher) {

    KrakenExceptionHandler exceptionHandler =
        new KrakenExceptionHandler(
            errorAttributes,
            webProperties.getResources(),
            serverCodecConfigurer,
            applicationContext,
            publisher);
    exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
    exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
    exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
    return exceptionHandler;
  }

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configure) {
    configure.defaultCodecs().maxInMemorySize(BYTE_COUNT);
  }
}
