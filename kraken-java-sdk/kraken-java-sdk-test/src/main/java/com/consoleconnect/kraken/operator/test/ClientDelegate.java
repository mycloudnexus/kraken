package com.consoleconnect.kraken.operator.test;

import org.reactivestreams.Publisher;

@FunctionalInterface
public interface ClientDelegate<T> {

  Publisher<T> call();
}
