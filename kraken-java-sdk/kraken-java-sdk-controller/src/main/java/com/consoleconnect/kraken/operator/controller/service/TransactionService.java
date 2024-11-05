package com.consoleconnect.kraken.operator.controller.service;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TransactionService {

  @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
  public <R, T> R execInNewTransaction(Function<T, R> function, T param) {
    return function.apply(param);
  }
}
