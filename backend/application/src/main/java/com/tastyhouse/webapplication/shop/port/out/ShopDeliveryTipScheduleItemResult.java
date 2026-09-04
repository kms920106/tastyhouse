package com.tastyhouse.webapplication.shop.port.out;

/**
 * 시간대별 추가 배달팁 한 행(손님 화면) — 요일 표시명과 {@code "HH:mm"} 문구까지 완성해 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code ShopDeliveryTipScheduleResult}는 {@code dayType}을
 * <b>String</b>으로, 시각을 {@code LocalTime}으로 갖는다. 손님 응답은 요일 <b>표시명</b>과
 * {@code "HH:mm"} 문자열을 함께 내리는데, 표시명을 얻으려면 {@code DayType.from(String)}으로 승격한 뒤
 * accessor를 읽어야 한다 — 그 {@code from(...)}은 web-api에서 호출할 수 없는 도메인 enum 메서드이므로
 * (읽기 accessor 3종만 허용) 승격과 포맷을 서비스에 남기고 강등된 결과를 담는다.
 */
public record ShopDeliveryTipScheduleItemResult(
    String dayType,
    String dayTypeDescription,
    String startTime,
    String endTime,
    int tipAmount
) {
}
