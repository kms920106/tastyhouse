package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ShopChangeValueFormatter {
    private static final int MAX_SNAPSHOT_ITEMS = 20;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ShopChangeValueFormatter() {
    }

    public static String amount(Integer amount) {
        return amount == null ? unset() : String.format("%,d원", amount);
    }

    public static String amount(BigDecimal amount) {
        return amount == null ? unset() : String.format("%,d원", amount.longValue());
    }

    public static String distanceKm(BigDecimal distanceKm) {
        return distanceKm == null ? unset() : distanceKm.stripTrailingZeros().toPlainString() + "km";
    }

    public static String time(LocalTime time) {
        return time == null ? unset() : time.format(TIME_FORMATTER);
    }

    public static String timeRange(LocalTime from, LocalTime to) {
        return time(from) + "~" + time(to);
    }

    public static String date(LocalDate date) {
        return date == null ? unset() : date.format(DATE_FORMATTER);
    }

    public static String dateRange(LocalDate from, LocalDate to) {
        return date(from) + "~" + date(to);
    }

    public static String enabled(Boolean enabled) {
        if (enabled == null) {
            return unset();
        }
        return enabled ? "사용" : "미사용";
    }

    public static String unset() {
        return "미설정";
    }

    public static String snapshot(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "없음";
        }
        if (lines.size() <= MAX_SNAPSHOT_ITEMS) {
            return String.join("\n", lines);
        }
        String head = String.join("\n", lines.subList(0, MAX_SNAPSHOT_ITEMS));
        return head + "\n외 " + (lines.size() - MAX_SNAPSHOT_ITEMS) + "건";
    }
}
