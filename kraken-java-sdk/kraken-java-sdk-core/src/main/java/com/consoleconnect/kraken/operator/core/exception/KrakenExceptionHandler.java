package com.consoleconnect.kraken.operator.core.exception;

import com.consoleconnect.kraken.operator.core.event.ExceptionEvent;
import com.consoleconnect.kraken.operator.core.toolkit.StringUtils;
import java.util.*;
import lombok.Getter;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Order(Integer.MIN_VALUE)
public class KrakenExceptionHandler extends AbstractErrorWebExceptionHandler {
  private static final int REASON_LENGTH_UPPER_LIMIT = 255;
  @Getter List<TriConsumer<ServerRequest, Object, HttpStatusCode>> callbackList = new ArrayList<>();
  private static final String ERR_MSG_FORMAT = "time:%s, error: %s, path:%s";
  private final ApplicationEventPublisher publisher;

  public KrakenExceptionHandler(
      final ErrorAttributes errorAttributes,
      final WebProperties.Resources resources,
      final ServerCodecConfigurer serverCodecConfigurer,
      final ApplicationContext applicationContext,
      final ApplicationEventPublisher publisher) {
    super(errorAttributes, resources, applicationContext);
    this.publisher = publisher;
    setMessageReaders(serverCodecConfigurer.getReaders());
    setMessageWriters(serverCodecConfigurer.getWriters());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    ErrorAttributeOptions options =
        ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.MESSAGE,
            ErrorAttributeOptions.Include.EXCEPTION,
            ErrorAttributeOptions.Include.BINDING_ERRORS);
    Map<String, Object> map = getErrorAttributes(request, options);
    Throwable throwable = getError(request);
    HttpStatusCode httpStatus = determineHttpStatus(throwable);
    String message =
        String.format(ERR_MSG_FORMAT, map.get("timestamp"), map.get("error"), map.get("path"));

    Object errorBody = generateBody(httpStatus, message, throwable);
    publisher.publishEvent(new ExceptionEvent(errorBody, httpStatus.value(), request.exchange()));
    return ServerResponse.status(httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorBody))
        .doOnNext(
            serverResponse ->
                callbackList.forEach(action -> action.accept(request, errorBody, httpStatus)));
  }

  public Object generateBody(HttpStatusCode httpStatus, String message, Throwable throwable) {
    ErrorResponse errorResponse = new ErrorResponse();
    String code = ErrorResponse.ErrorMapping.defaultMsg(httpStatus.value(), throwable.getMessage());
    errorResponse.setCode(code);
    // The max length of reason is 255.
    String reason = StringUtils.truncate(throwable.getMessage(), REASON_LENGTH_UPPER_LIMIT);
    errorResponse.setReason(reason);

    errorResponse.setMessage(
        (Objects.isNull(throwable.getCause()) ? message : throwable.getCause().getMessage()));
    errorResponse.setReferenceError(reason);
    if (httpStatus.value() != HttpStatus.UNPROCESSABLE_ENTITY.value()) {
      return errorResponse;
    }
    return List.of(errorResponse);
  }

  private HttpStatusCode determineHttpStatus(Throwable throwable) {
    if (throwable instanceof ResponseStatusException responseStatusException) {
      return responseStatusException.getStatusCode();
    } else if (throwable instanceof KrakenException krakenException) {
      return HttpStatus.valueOf(krakenException.getCode());
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public void registerCallback(TriConsumer<ServerRequest, Object, HttpStatusCode> consumer) {
    callbackList.add(consumer);
  }
}
