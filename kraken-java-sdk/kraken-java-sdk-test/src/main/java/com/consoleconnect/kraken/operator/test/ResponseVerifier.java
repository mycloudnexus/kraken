package com.consoleconnect.kraken.operator.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

public class ResponseVerifier {

  private final Publisher<?> publisher;
  private final MockServer mockServer;

  public ResponseVerifier(MockServer mockServer, Publisher<?> publisher) {
    assertThat("Publisher is null", publisher, is(notNullValue()));
    this.publisher = publisher;
    this.mockServer = mockServer;
  }

  public MockServer expectErrors(Predicate<Throwable> predicate) {
    StepVerifier.create(publisher).expectErrorMatches(predicate).verify();
    return mockServer;
  }

  public <T> MockServer expectResponse(T response) {
    StepVerifier.create((Publisher<T>) publisher).expectNext(response).verifyComplete();
    return mockServer;
  }

  public MockServer expectNoContent() {
    StepVerifier.create(publisher).verifyComplete();
    return mockServer;
  }
}
