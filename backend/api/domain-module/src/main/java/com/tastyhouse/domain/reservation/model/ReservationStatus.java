package com.tastyhouse.domain.reservation.model;

import java.util.Set;

public enum ReservationStatus {
    PENDING,    // 예약 대기 (사용자 신청, 점주 승인 전)
    CONFIRMED,  // 예약 확정 (점주 승인)
    REJECTED,   // 예약 거절 (점주)
    CANCELED,   // 예약 취소 (사용자)
    COMPLETED;  // 방문 완료

    /**
     * "회원당 같은 가게·같은 날짜 1예약" 차단 판정의 <b>단일 원천</b>.
     *
     * <p>취소(CANCELED)·거절(REJECTED)된 예약은 재예약을 막지 않는다. 이 판정은 실제 차단
     * (write 포트 {@code ReservationRepository#existsBlockingByMemberShopDate})과 화면 표시
     * (query DAO {@code ReservationQueryDao#existsBlockingReservation})가 함께 참조하므로,
     * 상태 목록을 인프라에 복제하지 말고 반드시 이 메서드/{@link #blockingStatuses()}를 쓴다.
     */
    public boolean isBlocking() {
        return this == PENDING || this == CONFIRMED || this == COMPLETED;
    }

    /**
     * {@link #isBlocking()}에 해당하는 상태 집합 — QueryDSL {@code in} 절 등 목록이 필요한 곳에서 쓴다.
     */
    public static Set<ReservationStatus> blockingStatuses() {
        return BLOCKING;
    }

    private static final Set<ReservationStatus> BLOCKING = Set.of(PENDING, CONFIRMED, COMPLETED);
}
