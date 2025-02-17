package com.consoleconnect.kraken.operator.core.toolkit;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DateTimeTest {

  @Test
  void givenDateString_whenCreating_thenOK() {
    ZonedDateTime dateTime = DateTime.of("2025-02-14T10:15:30+02:00[Europe/Athens]");
    Assertions.assertNotNull(dateTime);
  }

  @Test
  void givenDays_whenPlus_thenOK() {
    ZonedDateTime dateTime = DateTime.futureDaysInUTC(1);
    Assertions.assertNotNull(dateTime);

    ZonedDateTime dateTime1 = DateTime.futureInUTC(ChronoUnit.DAYS, 1);
    Assertions.assertNotNull(dateTime1);

    long d = DateTime.futureInDay(1);
    Assertions.assertTrue(d > 0);

    String s1 = DateTime.nowInUTCString();
    Assertions.assertNotNull(s1);

    String s2 = DateTime.nowInUTCFormatted(1, ChronoUnit.DAYS);
    Assertions.assertNotNull(s2);

    String s3 = DateTime.format(dateTime1);
    Assertions.assertNotNull(s3);
  }
}
