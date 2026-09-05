package com.tastyhouse.external.payment.toss;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TossPaymentUtils {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentUtils.class);

    private TossPaymentUtils() {
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return offsetDateTime.toLocalDateTime();
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", dateTimeStr, e);
            return null;
        }
    }

    public static String mapIssuerCodeToCardCompany(String issuerCode) {
        if (issuerCode == null) {
            return null;
        }
        return switch (issuerCode) {
            case "3K" -> "기업BC";
            case "46" -> "광주";
            case "71" -> "롯데";
            case "30" -> "KDB산업";
            case "31" -> "BC";
            case "51" -> "삼성";
            case "38" -> "새마을";
            case "41" -> "신한";
            case "62" -> "신협";
            case "36" -> "씨티";
            case "33" -> "우리";
            case "37" -> "우체국";
            case "39" -> "저축";
            case "35" -> "전북";
            case "42" -> "제주";
            case "15" -> "카카오뱅크";
            case "3A" -> "케이뱅크";
            case "24" -> "토스뱅크";
            case "21" -> "하나";
            case "61" -> "현대";
            case "11" -> "KB국민";
            case "91" -> "NH농협";
            case "34" -> "Sh수협";
            default -> issuerCode;
        };
    }
}
