package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.google.common.collect.Maps;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AppTest {
  @Test
  void testApp() {
    HashMap<@Nullable Object, @Nullable Object> objectObjectHashMap = Maps.newHashMap();

    objectObjectHashMap.put("buildId", UUID.randomUUID().toString());
    objectObjectHashMap.put("deployId", UUID.randomUUID().toString());
    objectObjectHashMap.put("buildName", "build-version-1");
    System.out.println(JsonToolkit.toJson(HttpResponse.ok(objectObjectHashMap)));
    String date = DateTime.formatCompact(ZonedDateTime.now());
    Assertions.assertNotNull(date);
  }
}
