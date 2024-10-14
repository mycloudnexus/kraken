package com.consoleconnect.kraken.operator.core.toolkit;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class Paging<T> {

  List<T> data = new LinkedList<>();
  Long total;
  Integer page;
  Integer size;
}
