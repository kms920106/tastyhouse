package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 변경이력 요약 문자열에 쓰는 공통 포맷 유틸.
 *
 * <p>금액·시간·날짜·컬렉션 결합처럼 <b>여러 기록 지점이 똑같이 필요한 표기</b>만 여기 둔다. "영업시간 1행을
 * 어떻게 한 줄로 요약하는가"처럼 도메인마다 다른 조립은 각 도메인 서비스의 {@code private describeXxx(...)}가
 * 담당한다 — 29종 중분류의 표기 규칙을 한 클래스에 모으면 그 자체가 새로운 신 클래스가 된다.
 *
 * <p>이력은 append-only 불변 데이터이므로 여기서 만든 문자열은 기록 시점의 표현으로 그대로 굳는다.
 * 표기를 바꿔도 과거 행은 바뀌지 않는다(의도된 동작이다).
 */
public final class ShopChangeValueFormatter {

    /** 컬렉션 스냅샷에 그대로 나열할 최대 항목 수. 초과분은 "외 N건"으로 축약한다. */
    private static final int MAX_SNAPSHOT_ITEMS = 20;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ShopChangeValueFormatter() {
    }

    /**
     * 금액을 천 단위 구분 기호와 "원"을 붙여 표기한다. null이면 "미설정".
     */
    public static String amount(Integer amount) {
        return amount == null ? unset() : String.format("%,d원", amount);
    }

    /**
     * 금액을 천 단위 구분 기호와 "원"을 붙여 표기한다. null이면 "미설정".
     */
    public static String amount(BigDecimal amount) {
        return amount == null ? unset() : String.format("%,d원", amount.longValue());
    }

    /**
     * 거리를 km 단위로 표기한다. 소수점 뒤 불필요한 0은 떼어낸다(2.50 → 2.5km).
     */
    public static String distanceKm(BigDecimal distanceKm) {
        return distanceKm == null ? unset() : distanceKm.stripTrailingZeros().toPlainString() + "km";
    }

    /**
     * 시각을 {@code HH:mm}으로 표기한다. null이면 "미설정".
     */
    public static String time(LocalTime time) {
        return time == null ? unset() : time.format(TIME_FORMATTER);
    }

    /**
     * 시간 구간을 {@code HH:mm~HH:mm}으로 표기한다.
     */
    public static String timeRange(LocalTime from, LocalTime to) {
        return time(from) + "~" + time(to);
    }

    /**
     * 날짜를 {@code yyyy-MM-dd}로 표기한다. null이면 "미설정".
     */
    public static String date(LocalDate date) {
        return date == null ? unset() : date.format(DATE_FORMATTER);
    }

    /**
     * 날짜 구간을 {@code yyyy-MM-dd~yyyy-MM-dd}로 표기한다.
     */
    public static String dateRange(LocalDate from, LocalDate to) {
        return date(from) + "~" + date(to);
    }

    /**
     * 켜짐/꺼짐 설정을 한글로 표기한다. null이면 "미설정".
     */
    public static String enabled(Boolean enabled) {
        if (enabled == null) {
            return unset();
        }
        return enabled ? "사용" : "미사용";
    }

    /**
     * 값이 없음을 나타내는 표기. 빈 문자열이 아니라 명시적 라벨을 쓴다 — 화면에서 "값이 비었다"와
     * "이력에 값이 안 담겼다"가 구분되어야 한다.
     */
    public static String unset() {
        return "미설정";
    }

    /**
     * 컬렉션 요약 줄들을 줄바꿈으로 결합한다. 비어 있으면 "없음"을 반환한다 — replace-all 변경에서
     * "전부 삭제해 빈 컬렉션이 됨"이 빈 문자열과 구분되어야 한다.
     *
     * <p>{@value #MAX_SNAPSHOT_ITEMS}건을 넘으면 뒤를 "외 N건"으로 축약한다. 배달가능지역 bulk 설정처럼
     * 수백 건이 한 번에 바뀌는 경로가 있어, 축약하지 않으면 한 행이 화면을 다 덮는다.
     */
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
