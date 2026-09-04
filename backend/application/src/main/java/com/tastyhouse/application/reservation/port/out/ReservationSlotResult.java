package com.tastyhouse.application.reservation.port.out;

import java.time.LocalTime;

/**
 * 슬롯 가용 정보 — 시간대별 잔여 수와 예약 가능 여부.
 *
 * <p><b>챕터 10</b>에서 신설. 이 값은 읽기 포트의 투영이 아니라 <b>서비스가 계산한 결과</b>라 공유
 * 읽기 계약 패키지에 형제를 둘 수 없다 — 잔여 수는 슬롯 점유 조회에 {@code SlotPolicy.CAPACITY_PER_SLOT}
 * 기본값을 얹은 것이고, {@code available}은 잔여·과거 여부(KST 시계)·본인 차단 예약 존재를 함께 본
 * 판정이다. 시계와 도메인 정책을 읽는 판정이므로 web-api로 내릴 수 없다.
 *
 * <p>대응 표현 계약은 {@code ReservationSlot}이다(접미어 없는 이름을 승격 전 그대로 유지).
 */
public record ReservationSlotResult(
    LocalTime time,
    int remaining,
    boolean available
) {
}
