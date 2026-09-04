package com.tastyhouse.application.reservation.port.out;

import java.time.LocalDateTime;

/**
 * 예약 완료 화면 상세 — 예약 일시를 날짜·시간 결합값으로 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code ReservationResult}는 예약 날짜와 시간을 별도
 * 필드로 갖는데 이 화면의 계약은 둘을 합친 {@code reservationAt} 하나이므로, 결합
 * ({@code LocalDateTime.of(date, time)})을 서비스에 남기고 그 결과를 담아 넘긴다.
 */
public record ReservationCompleteDetailResult(
    Long id,
    String shopName,
    String shopImageUrl,
    LocalDateTime reservationAt,
    Integer partySize
) {
}
