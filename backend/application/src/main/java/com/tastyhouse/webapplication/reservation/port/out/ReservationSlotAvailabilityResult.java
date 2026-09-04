package com.tastyhouse.webapplication.reservation.port.out;

import java.time.LocalDate;
import java.util.List;

/**
 * 날짜별 슬롯 가용성 — 조회 날짜와 본인 차단 예약 존재 여부, 슬롯 목록.
 *
 * <p><b>챕터 10</b>에서 신설. 거처의 근거는 {@link ReservationSlotResult}와 같다 — 두 번의 포트 호출
 * (슬롯 점유·차단 예약 존재)과 도메인 정책을 합성한 계산 결과다.
 */
public record ReservationSlotAvailabilityResult(
    LocalDate date,
    boolean hasMyReservation,
    List<ReservationSlotResult> slots
) {
}
