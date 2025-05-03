package com.olh.feeds.core.utils;

import java.util.UUID;

public interface GeneratorUtils {
  static String generateFileName(String filename) {
    return String.valueOf(DateUtils.getMillisSecond()).concat(filename);
  }

  static String generateUUID() {
    return UUID.randomUUID().toString();
  }
}
