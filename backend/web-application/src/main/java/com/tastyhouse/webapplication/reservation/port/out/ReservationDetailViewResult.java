package com.tastyhouse.webapplication.reservation.port.out;

import java.time.LocalDateTime;

/**
 * 예약 상세(예약자 정보 포함) — 예약 일시를 날짜·시간 결합값으로 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. 거처와 근거는 {@link ReservationCompleteDetailResult}와 같다 — 공유
 * 읽기 계약 {@code ReservationDetailResult}가 날짜·시간을 나눠 갖는 반면 이 화면의 계약은 합친
 * {@code reservationAt} 하나이므로, 결합을 서비스에 남기고 결과를 담아 넘긴다. 상태 enum도 서비스에서
 * String으로 강등한다.
 */
public record ReservationDetailViewResult(
    Long id,
    Long shopId,
    String shopName,
    String shopImageUrl,
    String shopRoadAddress,
    String shopLotAddress,
    Long memberId,
    String reserverName,
    String reserverPhoneNumber,
    String reserverEmail,
    LocalDateTime reservationAt,
    Integer partySize,
    String status,
    String request,
    LocalDateTime createdAt
) {
}
