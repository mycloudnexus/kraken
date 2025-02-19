package com.consoleconnect.kraken.operator.core.toolkit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * @author dxiong
 */
public class DateTime {

  private DateTime() {}

  private static final String ZONE_UTC = "UTC";
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter DEFAULT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter COMPACT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  public static ZonedDateTime of(String datetime) {
    return ZonedDateTime.parse(datetime);
  }

  public static ZonedDateTime nowInUTC() {
    return ZonedDateTime.now(ZoneId.of(ZONE_UTC));
  }

  public static String nowInUTCString() {
    return nowInUTC().toInstant().toString();
  }

  public static ZonedDateTime futureDaysInUTC(int days) {
    return ZonedDateTime.ofInstant(Instant.now().plus(days, ChronoUnit.DAYS), ZoneId.of(ZONE_UTC));
  }

  public static ZonedDateTime futureInUTC(ChronoUnit unit, int value) {
    return ZonedDateTime.ofInstant(Instant.now().plus(value, unit), ZoneId.of(ZONE_UTC));
  }

  public static long futureInDay(int days) {
    return Instant.now().plus(days, ChronoUnit.DAYS).toEpochMilli();
  }

  public static String nowInUTCFormatted() {
    return ZonedDateTime.now(ZoneId.of(ZONE_UTC)).format(FORMATTER);
  }

  public static String nowInUTCFormatted(int amount, TemporalUnit unit) {
    return ZonedDateTime.now(ZoneId.of(ZONE_UTC)).plus(amount, unit).format(FORMATTER);
  }

  public static String format(ZonedDateTime zonedDateTime) {
    return DEFAULT_FORMATTER.format(zonedDateTime);
  }

  public static String formatCompact(ZonedDateTime zonedDateTime) {
    return COMPACT_FORMATTER.format(zonedDateTime);
  }
}
