package com.olh.feeds.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
  private static final DateTimeFormatter YYYY_MM_DD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter MM_DD_YYYY_HH_MM_SS_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
  private static final String TIMEZONE_UTC7 = "Asia/Ho_Chi_Minh";


  public static String getCurrentDateString() {
    return LocalDate.now().toString();
  }

  public static long convertToTimestamp(String dateTimeStr) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    Date date = sdf.parse(dateTimeStr);

    return date.getTime();
  }

  public static String convertToMillisSecond(Long time) {
    try {
      Date date = new Date(time);
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      return sdf.format(date);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static long convertToMillisSecond(String dateTime) {
    try {
      LocalDate localDate = LocalDate.parse(dateTime, YYYY_MM_DD_FORMAT);
      return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static long getMillisSecond() {
    return Calendar.getInstance().getTimeInMillis();
  }

  public static Long getStartOfDayInTimestamp(String dateFrom) {
    if (dateFrom == null) return null;
    try {
      LocalDate date = LocalDate.parse(dateFrom, YYYY_MM_DD_FORMAT);
      LocalDateTime endOfDay = date.atTime(0, 0, 0);
      return endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  public static String convertTimestampToDateTime(long timestampMillis) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
    return MM_DD_YYYY_HH_MM_SS_FORMAT.format(dateTime);
  }

  public static String convertTimestampToDateTime(LocalDateTime dateTime) {
    return MM_DD_YYYY_HH_MM_SS_FORMAT.format(dateTime);
  }

  public static String convertDateFormat(String inputDate) {
    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    LocalDate date = LocalDate.parse(inputDate, inputFormatter);
    return date.format(outputFormatter);
  }

  public static String formatTimestamp(long timestamp) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy H'h'mm");

    return dateTime.format(formatter);
  }
}
