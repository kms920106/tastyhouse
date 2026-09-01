package com.tastyhouse.webapplication.shop.port.out;

import java.time.LocalDateTime;

/**
 * 예약 가능 수령시간 슬롯 한 칸 — 표시 문구까지 완성해 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. {@code label}·{@code dayLabel}은 <b>서버가 만든 완성 문구</b>다 — 배달은
 * 범위({@code "오후 6:00~오후 6:30"}), 포장은 단일 시각이라는 분기가 도메인 정책
 * ({@code ScheduledOrderPolicy#isRangeSlot})에 달려 있고, 날짜 구분은 오늘 기준 상대 표기라 시계를
 * 읽는다. 둘 다 web-api가 할 수 없는 계산이므로 서비스에 남기고 결과만 담는다.
 */
public record ScheduledOrderSlotItemResult(
    LocalDateTime startAt,
    LocalDateTime endAt,
    String label,
    String dayLabel
) {
}
