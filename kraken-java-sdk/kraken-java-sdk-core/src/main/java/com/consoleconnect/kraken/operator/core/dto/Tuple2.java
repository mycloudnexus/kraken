package com.consoleconnect.kraken.operator.core.dto;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.util.Assert;

public record Tuple2(String field, String value) {
  public static Tuple2 of(String field, String value) {
    return new Tuple2(field, value);
  }

  public static List<Tuple2> ofList(String field, String value, String... kvs) {
    Assert.isTrue(kvs.length % 2 == 0, "");
    List<Tuple2> tuple2List =
        Lists.partition(Arrays.stream(kvs).toList(), 2).stream()
            .map(list -> new Tuple2(list.get(0), list.get(1)))
            .toList();
    ArrayList<Tuple2> tuple2s = Lists.newArrayList(tuple2List);
    tuple2s.add(new Tuple2(field, value));
    return tuple2s;
  }
}
