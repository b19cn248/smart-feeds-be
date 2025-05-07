package com.olh.feeds.service.impl;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DateTimeUtils {

    // Formatter cho định dạng tiêu chuẩn RFC 822 với năm 4 chữ số
    private static final DateTimeFormatter RFC_822_FULL_YEAR = DateTimeFormatter.ofPattern(
            "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    // Formatter đặc biệt cho định dạng với năm 2 chữ số, với pivotYear = 2000
    private static final DateTimeFormatter RFC_822_TWO_DIGIT_YEAR = new DateTimeFormatterBuilder()
            .appendPattern("EEE, dd MMM ")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .appendPattern(" HH:mm:ss Z")
            .toFormatter(Locale.ENGLISH);

    public static LocalDateTime parseRFC822DateSafely(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Thử parse với định dạng năm 4 chữ số trước
            ZonedDateTime zdt = ZonedDateTime.parse(dateStr, RFC_822_FULL_YEAR);
            return zdt.toLocalDateTime();
        } catch (Exception e) {
            try {
                // Thử parse với định dạng năm 2 chữ số nếu cách trên thất bại
                ZonedDateTime zdt = ZonedDateTime.parse(dateStr, RFC_822_TWO_DIGIT_YEAR);
                return zdt.toLocalDateTime();
            } catch (Exception e2) {
                // Log lỗi hoặc xử lý theo cách khác
                System.err.println("Không thể parse ngày: " + dateStr);
                return null;
            }
        }
    }
}
